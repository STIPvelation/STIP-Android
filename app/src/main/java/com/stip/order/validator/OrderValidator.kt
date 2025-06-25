package com.stip.stip.order

import android.content.Context
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import java.text.DecimalFormat
import android.util.Log

data class OrderParams(
    val limitPriceStr: String?,
    val quantityOrTotalStr: String?,
    val triggerPriceStr: String?,
    val isMarketOrder: Boolean,
    val isReservedOrder: Boolean,
    val isInputModeTotalAmount: Boolean
)

class OrderValidator(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val getCurrentPrice: () -> Float,
    private val getCurrentTicker: () -> String?,
    private val availableUsdBalance: () -> Double,
    private val heldAssetQuantity: () -> Double,
    private val feeRate: Double,
    private val minimumOrderValue: Double,
    private val numberParseFormat: DecimalFormat,
    private val fixedTwoDecimalFormatter: DecimalFormat,
    private val showToast: (String) -> Unit,
    private val showErrorDialog: (titleResId: Int, message: String, colorResId: Int) -> Unit
) {
    companion object {
        private const val TAG = "OrderValidator"
    }

    fun validateOrder(params: OrderParams, isBuyOrder: Boolean): Boolean {
        val parsedInputs = parseAndValidateBasicInputs(params) ?: return false
        val limitPrice: Double? = parsedInputs.first
        val quantityOrTotal: Double = parsedInputs.second
        val triggerPrice: Double? = parsedInputs.third

        var quantity: Double? = null
        var grossTotalValue: Double? = null

        if (params.isMarketOrder && isBuyOrder) {
            grossTotalValue = quantityOrTotal
            quantity = null
        } else if (params.isInputModeTotalAmount && !params.isMarketOrder) {
            grossTotalValue = quantityOrTotal
            if (limitPrice != null && limitPrice > 0) {
                quantity = grossTotalValue / limitPrice
            } else {
                Log.e(TAG, "Validation failed in quantity calc from total: Invalid price.")
                showErrorDialog(
                    R.string.dialog_title_error_order,
                    context.getString(R.string.toast_invalid_price_for_total),
                    R.color.dialog_title_error_generic
                )
                return false
            }
        } else {
            quantity = quantityOrTotal
            if (!params.isMarketOrder && limitPrice != null) {
                grossTotalValue = limitPrice * quantity
            } else if (params.isMarketOrder && !isBuyOrder) {
                grossTotalValue = quantity * getCurrentPrice().toDouble()
            }
        }

        if (!validateOrderRules(
                limitPrice,
                quantity,
                grossTotalValue,
                triggerPrice,
                isBuyOrder,
                params.isMarketOrder,
                params.isReservedOrder
            )
        ) {
            return false
        }

        if (!validateAvailability(quantity, grossTotalValue, isBuyOrder, params.isMarketOrder)) {
            return false
        }

        return true
    }

    private fun parseAndValidateBasicInputs(params: OrderParams): Triple<Double?, Double, Double?>? {
        var price: Double? = null
        var quantityOrTotal: Double?
        var triggerPrice: Double? = null

        if (!params.isMarketOrder) {
            price = try {
                numberParseFormat.parse(params.limitPriceStr ?: "")?.toDouble()
            } catch (e: Exception) {
                null
            }
            if (price == null || price <= 0.0) {
                Log.w(TAG, "Validation failed: Invalid limit price.")
                showToast(context.getString(R.string.toast_invalid_price))
                return null
            }
        }

        quantityOrTotal = try {
            numberParseFormat.parse(params.quantityOrTotalStr ?: "")?.toDouble()
        } catch (e: Exception) {
            null
        }
        if (quantityOrTotal == null || quantityOrTotal <= 0.0) {
            Log.w(TAG, "Validation failed: Invalid quantity/total input.")
            val messageResId =
                if (params.isInputModeTotalAmount) R.string.toast_enter_total_amount else R.string.toast_enter_quantity
            showToast(context.getString(messageResId))
            return null
        }

        if (params.isReservedOrder) {
            triggerPrice = try {
                numberParseFormat.parse(params.triggerPriceStr ?: "")?.toDouble()
            } catch (e: Exception) {
                null
            }
            if (triggerPrice == null || triggerPrice <= 0.0) {
                Log.w(TAG, "Validation failed: Invalid trigger price.")
                showToast(context.getString(R.string.toast_invalid_trigger_price))
                return null
            }
        }

        return Triple(price, quantityOrTotal, triggerPrice)
    }


    private fun validateOrderRules(
        price: Double?,
        quantity: Double?,
        grossValue: Double?,
        triggerPrice: Double?,
        isBuyOrder: Boolean,
        isMarketOrder: Boolean,
        isReservedOrder: Boolean
    ): Boolean {
        val minOrderDisplayValue = fixedTwoDecimalFormatter.format(minimumOrderValue)
        val minOrderMessage =
            context.getString(R.string.toast_minimum_order_violation, minOrderDisplayValue)

        val valueToCheck = when {
            isMarketOrder && isBuyOrder -> grossValue
            isMarketOrder && !isBuyOrder -> quantity?.let { it * getCurrentPrice().toDouble() }
            else -> grossValue
        }

        if (valueToCheck != null && valueToCheck < minimumOrderValue) {
            Log.w(TAG, "Validation failed: Minimum order value not met.")
            showErrorDialog(
                R.string.dialog_title_warning_order,
                minOrderMessage,
                R.color.dialog_title_error_generic
            )
            return false
        }

        if (isReservedOrder && price != null && triggerPrice != null) {
            if (isBuyOrder && triggerPrice < price) {
                Log.w(TAG, "Validation failed: Buy reserved trigger price condition.")
                showErrorDialog(
                    R.string.reserved_order_error_title,
                    context.getString(R.string.buy_trigger_price_error),
                    R.color.dialog_title_buy_error_red
                )
                return false
            } else if (!isBuyOrder && triggerPrice > price) {
                Log.w(TAG, "Validation failed: Sell reserved trigger price condition.")
                showErrorDialog(
                    R.string.reserved_order_error_title,
                    context.getString(R.string.sell_trigger_price_error),
                    R.color.dialog_title_sell_error_blue
                )
                return false
            }
        }
        return true
    }


    private fun validateAvailability(
        quantity: Double?,
        grossValue: Double?,
        isBuyOrder: Boolean,
        isMarketOrder: Boolean
    ): Boolean {
        val epsilon = 0.00000001

        if (isBuyOrder) {
            val requiredCost = when {
                isMarketOrder -> grossValue ?: 0.0
                grossValue != null -> grossValue * (1.0 + feeRate)
                else -> 0.0
            }
            val available = availableUsdBalance()

            Log.d(TAG, "Buy Check - Required: $requiredCost, Available: $available") // 값 확인 로그

            if (requiredCost > available + epsilon) {
                Log.w(
                    TAG,
                    "Validation failed: Insufficient funds. SHOWING DIALOG..."
                ) // <<<--- 로그 추가
                showErrorDialog(
                    R.string.dialog_title_error_order,
                    context.getString(R.string.buy_error_insufficient_funds),
                    R.color.dialog_title_buy_error_red
                )
                return false
            }
        } else {
            if (quantity == null || quantity <= 0.0) {
                Log.w(TAG, "Validation failed: Invalid quantity for sell.")
                showToast(context.getString(R.string.toast_enter_quantity))
                return false
            }
            val held = heldAssetQuantity()
            Log.d(TAG, "Sell Check - Required Qty: $quantity, Held Qty: $held") // 값 확인 로그

            if (quantity > held + epsilon) {
                Log.w(
                    TAG,
                    "Validation failed: Insufficient quantity. SHOWING DIALOG..."
                ) // <<<--- 로그 추가
                showErrorDialog(
                    R.string.dialog_title_error_order,
                    context.getString(R.string.sell_error_insufficient_quantity),
                    R.color.dialog_title_sell_error_blue
                )
                return false
            }
        }
        return true
    }
}
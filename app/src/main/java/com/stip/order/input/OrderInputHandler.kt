package com.stip.stip.order

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.signup.utils.PreferenceUtil
import java.text.DecimalFormat
import kotlin.math.floor

class OrderInputHandler(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val numberParseFormat: DecimalFormat,
    private val fixedTwoDecimalFormatter: DecimalFormat,
    private val getCurrentPrice: () -> Double,
    private val getFeeRate: () -> Double,
    private val availableUsdBalance: () -> Double,
    private val heldAssetQuantity: () -> Double,
    private val getCurrentTicker: () -> String?,
    private val getCurrentOrderType: () -> Int
) {

    private var calculatedMaxQty: Double = 0.0
    private enum class LastEdited { QUANTITY, TOTAL_AMOUNT, PRICE, NONE }
    private var lastEditedFocus: LastEdited = LastEdited.NONE

    val quantityTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateCalculatedTotal()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    val priceTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateCalculatedTotal()
            calculateAndDisplayMaxQuantity()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    val formatOnFocusLostListener = View.OnFocusChangeListener { view, hasFocus ->
        if (!hasFocus && view is EditText) {
            val currentText = view.text.toString()
            try {
                val number = if (currentText.isBlank()) 0.0 else (numberParseFormat.parse(currentText)?.toDouble() ?: 0.0)
                val watcher = getWatcherForEditText(view)
                watcher?.let { view.removeTextChangedListener(it) }
                view.setText(fixedTwoDecimalFormatter.format(number))
                watcher?.let { view.addTextChangedListener(it) }

                updateCalculatedTotal()
                if (view.id == R.id.editTextLimitPrice) {
                    calculateAndDisplayMaxQuantity()
                }
            } catch (e: Exception) {
                Log.e("OrderInputHandler", "Error formatting number on focus lost: $currentText", e)
                val watcher = getWatcherForEditText(view)
                watcher?.let { view.removeTextChangedListener(it) }
                view.setText(fixedTwoDecimalFormatter.format(0.0))
                watcher?.let { view.addTextChangedListener(it) }
                updateCalculatedTotal()
                if (view.id == R.id.editTextLimitPrice) {
                    calculateAndDisplayMaxQuantity()
                }
            }
        } else if (hasFocus && view is EditText) {
            when(view.id) {
                R.id.editTextQuantity -> lastEditedFocus = LastEdited.QUANTITY
                R.id.editTextLimitPrice -> lastEditedFocus = LastEdited.PRICE
            }
        }
    }

    private fun getWatcherForEditText(editText: EditText): TextWatcher? {
        return when(editText.id) {
            R.id.editTextQuantity -> quantityTextWatcher
            R.id.editTextLimitPrice -> priceTextWatcher
            else -> null
        }
    }

    fun setupInputListeners() {
        binding.editTextQuantity.addTextChangedListener(quantityTextWatcher)
        binding.editTextLimitPrice.addTextChangedListener(priceTextWatcher)
        binding.editTextQuantity.onFocusChangeListener = formatOnFocusLostListener
        binding.editTextLimitPrice.onFocusChangeListener = formatOnFocusLostListener
        binding.editTextTriggerPrice?.onFocusChangeListener = formatOnFocusLostListener
        setupTotalRowClickListener()
    }

    private fun setupTotalRowClickListener() {
        binding.rowCalculatedTotal.setOnClickListener {
            Log.d("OrderInputHandler", "Calculated Total Row clicked - Applying Max Quantity")
            val maxQty = calculateMaxQuantity()
            binding.editTextQuantity.setText(fixedTwoDecimalFormatter.format(maxQty.coerceAtLeast(0.0)))
            updateCalculatedTotal()
            showKeyboard(binding.editTextQuantity)
        }
        binding.editTextQuantity.setOnClickListener(null)
    }

    fun setupPriceAdjustmentButtons() {
        binding.buttonPricePlus.setOnClickListener {
            adjustPrice(+1)
            updateCalculatedTotal()
            calculateAndDisplayMaxQuantity()
        }
        binding.buttonPriceMinus.setOnClickListener {
            adjustPrice(-1)
            updateCalculatedTotal()
            calculateAndDisplayMaxQuantity()
        }
        binding.buttonTriggerPricePlus?.setOnClickListener { adjustTriggerPrice(+1) }
        binding.buttonTriggerPriceMinus?.setOnClickListener { adjustTriggerPrice(-1) }
    }

    private fun adjustPrice(delta: Int) {
        val currentText = binding.editTextLimitPrice.text?.toString()
        val current = parseDouble(currentText)
        val base = if (current <= 0.0) getCurrentPrice() else current
        val tick = getTick(base)
        val newPrice = (base + delta * tick).coerceAtLeast(tick)
        val formattedPrice = fixedTwoDecimalFormatter.format(newPrice)
        binding.editTextLimitPrice.setText(formattedPrice)
        if (getCurrentOrderType() == R.id.radio_reserved_order) {
            binding.editTextTriggerPrice?.setText(formattedPrice)
        }
    }

    private fun adjustTriggerPrice(delta: Int) {
        val editText = binding.editTextTriggerPrice ?: return
        val currentText = editText.text?.toString()
        val current = parseDouble(currentText)
        val base = if (current <= 0.0) getCurrentPrice() else current
        val tick = getTick(base)
        val newPrice = (base + delta * tick).coerceAtLeast(tick)
        editText.setText(fixedTwoDecimalFormatter.format(newPrice))
    }

    fun resetInputs() {
        lastEditedFocus = LastEdited.NONE
        resetEditText(binding.editTextLimitPrice)
        resetEditText(binding.editTextQuantity)
        binding.editTextTriggerPrice?.let { resetEditText(it) }
        binding.textCalculatedTotal.text = fixedTwoDecimalFormatter.format(0.0)
        calculatedMaxQty = 0.0
        updateUiForOrderTypeChange() // 리셋 시 현재 주문 유형에 맞게 UI 업데이트
        try {
            if (binding.spinnerAvailableQuantity.adapter?.count ?: 0 > 0) {
                binding.spinnerAvailableQuantity.setSelection(0, false)
            }
        } catch (e: Exception) { Log.e("OrderInputHandler", "Error resetting spinner", e) }
    }

    fun setupResetButton() {
        binding.buttonReset.setOnClickListener { resetInputs() }
    }

    private fun resetEditText(editText: EditText) {
        val watcher = getWatcherForEditText(editText)
        watcher?.let { editText.removeTextChangedListener(it) }
        editText.setText(fixedTwoDecimalFormatter.format(0.0))
        watcher?.let { editText.addTextChangedListener(it) }
    }

    fun setupQuantitySpinner() {
        val ctx = binding.root.context
        val options = listOf(
            ctx.getString(R.string.quantity_option_available),
            ctx.getString(R.string.quantity_option_max),
            ctx.getString(R.string.quantity_option_75),
            ctx.getString(R.string.quantity_option_50),
            ctx.getString(R.string.quantity_option_25),
            ctx.getString(R.string.quantity_option_10)
        )

        val adapter = ArrayAdapter(ctx, R.layout.custom_spinner_item_quantity, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAvailableQuantity.adapter = adapter

        binding.spinnerAvailableQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) return

                val factor = when (position) {
                    1 -> 1.0; 2 -> 0.75; 3 -> 0.5; 4 -> 0.25; 5 -> 0.10; else -> 0.0
                }

                val maxQty = calculateMaxQuantity()
                val targetQty = maxQty * factor

                val watcher = quantityTextWatcher
                binding.editTextQuantity.removeTextChangedListener(watcher)
                binding.editTextQuantity.setText(fixedTwoDecimalFormatter.format(targetQty.coerceAtLeast(0.0)))
                binding.editTextQuantity.addTextChangedListener(watcher)

                lastEditedFocus = LastEdited.QUANTITY
                updateCalculatedTotal()
                binding.spinnerAvailableQuantity.setSelection(0, false)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    fun updateCalculatedTotal() {
        val qtyStr = binding.editTextQuantity.text?.toString()
        val priceStr = binding.editTextLimitPrice.text?.toString()
        val qty = parseDouble(qtyStr)
        val price = parseDouble(priceStr)
        var displayTotal = 0.0

        try {
            if (price > 0 && qty > 0) {
                val isBuy = binding.tabLayoutOrderMode.selectedTabPosition == 0
                val isMarketOrder = getCurrentOrderType() == R.id.radio_market_order
                val grossAmount = qty * price
                val fee = getFeeRate()
                displayTotal = if (isBuy && !isMarketOrder) grossAmount * (1 + fee) else if (!isBuy) grossAmount * (1 - fee) else grossAmount
            }
        } catch (e: Exception) {
            Log.e("OrderInputHandler", "Error calculating total", e)
            displayTotal = 0.0
        }

        binding.textCalculatedTotal.text = fixedTwoDecimalFormatter.format(displayTotal.coerceAtLeast(0.0))
        binding.labelCalculatedTotal.text = context.getString(R.string.label_total_amount)
        binding.textUnitCalculatedTotal.text = context.getString(R.string.unit_usd)
    }

    // OrderInputHandler.kt 내부

    fun updateUiForOrderTypeChange() {
        val context = binding.root.context
        val orderType = getCurrentOrderType()
        val isMarketOrder = orderType == R.id.radio_market_order
        val isBuyTab = binding.tabLayoutOrderMode.selectedTabPosition == 0
        val isReservedOrder = orderType == R.id.radio_reserved_order

        // 가격 라벨 설정 (예약 주문 시 변경)
        binding.labelLimitPrice.text = if(isReservedOrder) context.getString(R.string.label_order_price1) else context.getString(R.string.label_price)
        // 총액(계산 결과) 라벨 및 단위는 항상 고정
        binding.labelCalculatedTotal.text = context.getString(R.string.label_total_amount)
        binding.textUnitCalculatedTotal.text = context.getString(R.string.unit_usd)

        // --- ▼▼▼ 매수 & 시장가 조건에 따른 수량 라벨/힌트/단위 변경 ▼▼▼ ---
        if (isMarketOrder && isBuyTab) {
            // --- 시장가 매수 ---
            binding.labelQuantity.text = context.getString(R.string.label_total_amount) // 라벨: "총액"으로 변경!
            binding.editTextQuantity.hint = context.getString(R.string.hint_enter_total_amount) // 힌트: "총액 입력"
            binding.textUnitQuantity.text = context.getString(R.string.unit_usd) // 단위: "USD"
            binding.textOrderAvailableUnit.visibility = View.VISIBLE // 최대 가능 수량 표시
            calculateAndDisplayMaxQuantity()
        } else {
            // --- 그 외 모든 경우 (수량 입력 모드) ---
            binding.labelQuantity.text = context.getString(R.string.label_quantity) // 라벨: "수량"으로 복원/유지
            binding.editTextQuantity.hint = context.getString(R.string.hint_enter_quantity) // 힌트: "수량 입력"
            binding.textUnitQuantity.text = getCurrentTicker() ?: "" // 단위: 티커
            binding.textOrderAvailableUnit.visibility = View.VISIBLE // 최대 가능 수량 표시
            calculateAndDisplayMaxQuantity()
        }
        // --- ▲▲▲ 매수 & 시장가 조건에 따른 수량 라벨/힌트/단위 변경 ▲▲▲ ---

        binding.editTextQuantity.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        updateCalculatedTotal() // 주문 유형 변경 시 총액 재계산 및 표시
    }


    fun handleOrderBookPriceClick(price: String) {
        try {
            val priceNum = numberParseFormat.parse(price)?.toDouble() ?: 0.0
            val formattedPrice = fixedTwoDecimalFormatter.format(priceNum)
            binding.editTextLimitPrice.setText(formattedPrice)

            if (getCurrentOrderType() == R.id.radio_reserved_order) {
                binding.editTextTriggerPrice?.setText(formattedPrice)
            }
            updateCalculatedTotal()
            calculateAndDisplayMaxQuantity()
        } catch (e: Exception) {
            Log.e("OrderInputHandler", "Error handling order book price click", e)
            binding.editTextLimitPrice.setText(fixedTwoDecimalFormatter.format(0.0))
            updateCalculatedTotal()
            calculateAndDisplayMaxQuantity()
        }
    }

    private fun calculateAndDisplayMaxQuantity() {
        val calculatedQty = calculateMaxQuantity()
        this.calculatedMaxQty = calculatedQty
        binding.textOrderAvailableUnit.visibility = View.VISIBLE
        binding.textOrderAvailableUnit.text = context.getString(
            R.string.order_max_quantity_format,
            fixedTwoDecimalFormatter.format(calculatedQty)
        )
    }


    private fun calculateMaxQuantity(): Double {
        // 로그인 상태 확인
        val isLoggedIn = PreferenceUtil.getMemberInfo() != null
        
        // 로그인하지 않은 경우 무조건 0 반환
        if (!isLoggedIn) {
            return 0.0
        }
        
        val selectedTab = binding.tabLayoutOrderMode.selectedTabPosition
        var maxQty = 0.0
        val isMarketOrder = getCurrentOrderType() == R.id.radio_market_order

        try {
            if (selectedTab == 1) {
                maxQty = heldAssetQuantity()
            } else {
                val priceToUse: Double?
                if (isMarketOrder) {
                    priceToUse = getCurrentPrice()
                } else {
                    val limitPriceText = binding.editTextLimitPrice.text?.toString()
                    priceToUse = parseDouble(limitPriceText).takeIf { it > 0.0 } ?: getCurrentPrice()
                }

                // Price is guaranteed to be positive at this point
                val balance = availableUsdBalance()
                val fee = getFeeRate()
                val baseAmount = balance / (1 + fee)
                maxQty = floor((baseAmount / priceToUse) * 100_000_000) / 100_000_000
            }
        } catch (e: Exception) {
            Log.e("OrderInputHandler", "Error calculating max quantity", e)
            maxQty = 0.0
        }
        return maxQty.coerceAtLeast(0.0)
    }

    private fun parseDouble(value: String?): Double {
        return try {
            if (value.isNullOrBlank()) 0.0 else (numberParseFormat.parse(value)?.toDouble() ?: 0.0)
        } catch (e: Exception) {
            Log.w("OrderInputHandler", "Failed to parse double: '$value'")
            0.0
        }
    }

    private fun getTick(base: Double): Double {
        return when {
            base < 1.0 -> 0.001
            base < 10.0 -> 0.01
            base < 100.0 -> 0.1
            base < 1000.0 -> 0.5
            else -> 1.0
        }.coerceAtLeast(0.001)
    }

    private fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
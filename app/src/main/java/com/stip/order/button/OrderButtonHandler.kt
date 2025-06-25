package com.stip.stip.order

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import java.text.DecimalFormat
import com.stip.stip.iphome.fragment.ConfirmOrderDialogFragment
import android.util.Log
import com.stip.stip.signup.login.LoginActivity
import com.stip.stip.signup.utils.PreferenceUtil

class OrderButtonHandler(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val validator: OrderValidator, // Validator 주입 확인
    private val numberParseFormat: DecimalFormat,
    private val fixedTwoDecimalFormatter: DecimalFormat,
    private val getCurrentPrice: () -> Float,
    private val getFeeRate: () -> Double,
    // isInputModeTotalAmount 파라미터 삭제됨
    private val currentTicker: () -> String?,
    private val minimumOrderValue: Double, // Validator 가 사용하므로 제거 가능성 있음
    private val availableUsdBalance: () -> Double, // Validator 가 사용하므로 제거 가능성 있음
    private val heldAssetQuantity: () -> Double, // Validator 가 사용하므로 제거 가능성 있음
    private val showToast: (String) -> Unit, // Validator 가 사용하므로 제거 가능성 있음
    private val showErrorDialog: (titleResId: Int, message: String, colorResId: Int) -> Unit, // Validator 가 사용하므로 제거 가능성 있음
    private val parentFragmentManager: FragmentManager
) {
    companion object {
        private const val TAG = "OrderButtonHandler"
    }

    fun setupOrderButtonClickListeners() {
        // 버튼 클릭 리스너 - 현재 탭에 따라 매수/매도 구분
        binding.buttonBuy.setOnClickListener {
            val isBuyOrder = binding.tabLayoutOrderMode.selectedTabPosition == 0
            handleButtonClick(isBuyOrder)
        }
    }
    
    private fun handleButtonClick(isBuyOrder: Boolean) {
        // 로그인 상태 확인
        val isLoggedIn = PreferenceUtil.getMemberInfo() != null
        
        if (!isLoggedIn) {
            // 비로그인 상태일 때 로그인 화면으로 이동
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        } else {
            // 로그인 상태일 때 기존과 동일하게 주문 처리
            gatherInputsAndValidate(isBuyOrder)
        }
    }

    private fun gatherInputsAndValidate(isBuyOrder: Boolean) {
        val selectedOrderTypeId = binding.radioGroupOrderType.checkedRadioButtonId
        val isMarketOrder = selectedOrderTypeId == R.id.radio_market_order
        val isReservedOrder = selectedOrderTypeId == R.id.radio_reserved_order

        // InputHandler로부터 isInputModeTotalAmount 상태 가져오기 (생성자에서 제거했으므로 필요 없음)
        // val isTotalMode = isInputModeTotalAmount() // 제거됨

        // OrderParams 생성 (Validator에 전달하기 위함)
        // isInputModeTotalAmount 는 Validator가 직접 알 필요 없음. 값만 전달.
        val orderParams = OrderParams(
            limitPriceStr = binding.editTextLimitPrice.text?.toString(),
            quantityOrTotalStr = binding.editTextQuantity.text?.toString(),
            triggerPriceStr = binding.editTextTriggerPrice?.text?.toString(),
            isMarketOrder = isMarketOrder,
            isReservedOrder = isReservedOrder,
            // isInputModeTotalAmount = isTotalMode // Validator 가 상태 직접 알 필요 없음
            // 만약 Validator가 이 상태를 알아야 한다면, InputHandler 에서 가져오는 람다를 Validator에 전달해야 함
            isInputModeTotalAmount = false // 임시: Validator가 이 파라미터를 받는 경우 대비 (Validator에서 제거 필요)
        )

        // Validator를 통해 유효성 검사 실행
        if (validator.validateOrder(orderParams, isBuyOrder)) {
            // 유효성 검사 통과 시, 확인 다이얼로그 표시 로직 실행
            prepareAndShowConfirmationDialog(orderParams, isBuyOrder)
        }
        // 유효성 검사 실패 시, Validator가 이미 오류 다이얼로그를 표시했으므로 여기서 추가 작업 없음
    }

    private fun prepareAndShowConfirmationDialog(params: OrderParams, isBuyOrder: Boolean) {
        var price: Double? = null
        var quantity: Double? = null
        var triggerPrice: Double? = null
        var quantityOrTotalInput: Double = 0.0 // 파싱된 수량 또는 총액 입력값

        var orderTypeText: String
        var priceConfirmStr: String
        var quantityConfirmStr: String
        var displayTotalValueStr: String
        var calculatedFee: Double
        var feeConfirmStr: String
        var triggerPriceConfirmStr: String? = null

        // 값 파싱 (Validator에서 이미 기본적인 null/0 체크는 통과했다고 가정)
        try {
            quantityOrTotalInput = numberParseFormat.parse(params.quantityOrTotalStr ?: "0")?.toDouble() ?: 0.0
            if (!params.isMarketOrder) {
                price = numberParseFormat.parse(params.limitPriceStr ?: "0")?.toDouble() ?: 0.0
            }
            if (params.isReservedOrder) {
                triggerPrice = numberParseFormat.parse(params.triggerPriceStr ?: "0")?.toDouble() ?: 0.0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing values for confirmation dialog", e)
            showErrorDialog(R.string.dialog_title_error_order, "주문 정보를 처리하는 중 오류가 발생했습니다.", R.color.dialog_title_error_generic)
            return
        }

        // 수량 및 총액 결정 (OrderParams의 isInputModeTotalAmount 사용 제거)
        // Validator에서 계산된 quantity/grossTotalValue를 반환받는 것이 더 효율적일 수 있음
        var grossTotalValue: Double?
        if (params.isMarketOrder && isBuyOrder) {
            grossTotalValue = quantityOrTotalInput
            quantity = null
            quantityConfirmStr = "--"
        } else {
            // 수량 입력 모드 또는 시장가 매도 (총액 입력 모드 개념 제거됨)
            quantity = quantityOrTotalInput
            quantityConfirmStr = fixedTwoDecimalFormatter.format(quantity)
            grossTotalValue = if (!params.isMarketOrder && price != null) {
                price * quantity
            } else if (params.isMarketOrder && !isBuyOrder) {
                quantity * getCurrentPrice().toDouble() // 시장가 매도 예상가
            } else {
                null
            }
        }

        // 확인 다이얼로그 표시 문자열 준비
        priceConfirmStr = if (price != null) fixedTwoDecimalFormatter.format(price) else context.getString(R.string.market_price)
        triggerPriceConfirmStr = triggerPrice?.let { fixedTwoDecimalFormatter.format(it) }

        orderTypeText = when {
            params.isReservedOrder && isBuyOrder -> context.getString(R.string.order_type_reserved_limit_buy)
            params.isReservedOrder && !isBuyOrder -> context.getString(R.string.order_type_reserved_limit_sell)
            params.isMarketOrder && isBuyOrder -> context.getString(R.string.order_type_market_buy)
            params.isMarketOrder && !isBuyOrder -> context.getString(R.string.order_type_market_sell)
            isBuyOrder -> context.getString(R.string.order_type_limit_buy)
            else -> context.getString(R.string.order_type_limit_sell)
        }

        val feeRate = getFeeRate()
        if (params.isMarketOrder && isBuyOrder) {
            calculatedFee = (grossTotalValue ?: 0.0) * feeRate / (1.0 + feeRate)
            displayTotalValueStr = fixedTwoDecimalFormatter.format(grossTotalValue ?: 0.0)
        } else if (params.isMarketOrder && !isBuyOrder) {
            calculatedFee = (grossTotalValue ?: 0.0) * feeRate // 예상가 기준 수수료
            displayTotalValueStr = context.getString(R.string.market_total)
        } else if (price != null && quantity != null){
            val calculatedGross = price * quantity
            calculatedFee = calculatedGross * feeRate
            val finalDisplayAmount = if (isBuyOrder) calculatedGross * (1.0 + feeRate) else calculatedGross * (1.0 - feeRate)
            displayTotalValueStr = fixedTwoDecimalFormatter.format(finalDisplayAmount)
        } else {
            calculatedFee = 0.0
            displayTotalValueStr = "--"
        }
        feeConfirmStr = fixedTwoDecimalFormatter.format(calculatedFee)

        // 확인 다이얼로그 표시
        ConfirmOrderDialogFragment.newInstance(
            isBuyOrder = isBuyOrder,
            tickerFull = "${currentTicker() ?: "N/A"}/USD",
            orderType = orderTypeText,
            priceValue = priceConfirmStr,
            quantityValue = quantityConfirmStr,
            totalValue = displayTotalValueStr,
            feeValue = feeConfirmStr,
            triggerPriceValue = triggerPriceConfirmStr
        ).show(parentFragmentManager, ConfirmOrderDialogFragment.Companion.TAG)
    }

    // validateOrderRules 및 validateAvailability 헬퍼 메서드 제거됨
}
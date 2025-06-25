package com.stip.stip.order // 패키지는 실제 위치에 맞게 조정하세요

import androidx.core.content.ContextCompat
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.util.OrderUtils
import com.stip.stip.signup.utils.PreferenceUtil

// 이 코드는 OrderUIInitializer 클래스 내부에 있다고 가정합니다.
class OrderUIInitializer(
    private val context: android.content.Context,
    private val binding: FragmentOrderContentBinding
) {

    // setupRadioGroupListener 메서드 수정
    fun setupRadioGroupListener(
        currentTicker: String?,
        // updateInputModeUI 파라미터 제거됨
        resetOrderInputsToZero: () -> Unit
    ) {
        binding.radioGroupOrderType.setOnCheckedChangeListener { _, _ ->
            // updateUiForOrderType 호출 시 updateInputModeUI 인자 제거
            updateUiForOrderType(currentTicker, resetOrderInputsToZero)
        }
        // 초기 상태 설정 시에도 updateInputModeUI 인자 제거
        updateUiForOrderType(currentTicker, resetOrderInputsToZero)
    }

    // updateUiForOrderType 메서드 정의 예시 (OrderUIInitializer 내부에 필요)
    // 실제 구현은 기존 OrderContentViewFragment의 updateUiForOrderType 로직을 참고하여 작성해야 합니다.
    private fun updateUiForOrderType(
        currentTicker: String?,
        // updateInputModeUI 파라미터 제거됨
        resetOrderInputsToZero: () -> Unit
    ) {
        if (_binding == null) return // _binding 대신 binding 사용
        val selectedOrderTypeId = binding.radioGroupOrderType.checkedRadioButtonId
        val isBuyTabSelected = binding.tabLayoutOrderMode.selectedTabPosition == 0

        resetOrderInputsToZero() // 입력값 초기화 콜백 호출

        // 기존 updateUiForOrderType 로직에서 UI 요소 가시성/텍스트 설정 부분만 남김
        // isInputModeTotalAmount 상태 관리 및 updateInputModeUI 호출 부분 제거
        when (selectedOrderTypeId) {
            R.id.radio_limit_order -> {
                binding.rowLimitPrice.visibility = android.view.View.VISIBLE
                binding.rowTriggerPrice?.visibility = android.view.View.GONE
                binding.rowQuantity.visibility = android.view.View.VISIBLE
                binding.rowCalculatedTotal.visibility = android.view.View.VISIBLE
                binding.labelLimitPrice.text = context.getString(R.string.label_price) // context 사용
                // 라벨/단위 설정은 OrderInputHandler의 updateInputModeUI 가 담당하므로 여기서 중복 설정 불필요
                // if (isBuyTabSelected) { ... } else { ... } 부분 제거 또는 단순화
            }
            R.id.radio_market_order -> {
                binding.rowLimitPrice.visibility = android.view.View.GONE
                binding.rowTriggerPrice?.visibility = android.view.View.GONE
                binding.rowQuantity.visibility = android.view.View.VISIBLE
                binding.rowCalculatedTotal.visibility = android.view.View.GONE
                // 라벨/단위 설정은 OrderInputHandler의 updateInputModeUI 가 담당하므로 여기서 중복 설정 불필요
            }
            R.id.radio_reserved_order -> {
                binding.rowTriggerPrice?.visibility = android.view.View.VISIBLE
                binding.rowLimitPrice.visibility = android.view.View.VISIBLE
                binding.rowQuantity.visibility = android.view.View.VISIBLE
                binding.rowCalculatedTotal.visibility = android.view.View.VISIBLE
                binding.labelLimitPrice.text = context.getString(R.string.label_order_price1) // context 사용
                // 라벨/단위 설정은 OrderInputHandler의 updateInputModeUI 가 담당하므로 여기서 중복 설정 불필요
            }
            else -> { // 기본값 (지정가와 동일하게 처리)
                binding.rowLimitPrice.visibility = android.view.View.VISIBLE
                binding.rowTriggerPrice?.visibility = android.view.View.GONE
                binding.rowQuantity.visibility = android.view.View.VISIBLE
                binding.rowCalculatedTotal.visibility = android.view.View.VISIBLE
                binding.labelLimitPrice.text = context.getString(R.string.label_price) // context 사용
                // 라벨/단위 설정은 OrderInputHandler의 updateInputModeUI 가 담당하므로 여기서 중복 설정 불필요
            }
        }
        // OrderInputHandler의 UI 업데이트 메서드 호출 (필요한 경우)
        // 예: inputHandler.updateInputModeUI() // -> 이 클래스가 inputHandler 를 직접 알 수는 없음. 콜백 등으로 처리해야 함.
        // 현재 구조에서는 RadioButton 변경 시 resetInputs() 만 호출하고,
        // EditText 의 hint/unit 등은 inputHandler 내부 로직 + 필요 시 외부에서 inputHandler.updateInputModeUI() 호출로 관리
    }


    // --- 다른 OrderUIInitializer 메서드들 ---
    // 예: setupTabLayoutColors, updateBuySellButtonAppearance 등
    fun setupTabLayoutColors(onTabSelectedAction: (Int) -> Unit) {
        val activeBuy = ContextCompat.getColor(context, R.color.tab_text_active_buy)
        val activeSell = ContextCompat.getColor(context, R.color.tab_text_active_sell)
        val activeHist = ContextCompat.getColor(context, R.color.tab_text_active_history)
        val inactive = ContextCompat.getColor(context, R.color.color_text_default)
        val initialPos = binding.tabLayoutOrderMode.selectedTabPosition
        for (i in 0 until binding.tabLayoutOrderMode.tabCount) {
            val tab = binding.tabLayoutOrderMode.getTabAt(i)
            val color = if (i == initialPos) {
                when (i) { 0 -> activeBuy; 1 -> activeSell; 2 -> activeHist; else -> inactive }
            } else inactive
            OrderUtils.setTabTextColor(context, tab, color) // OrderUtils 사용
        }
        binding.tabLayoutOrderMode.clearOnTabSelectedListeners()
        binding.tabLayoutOrderMode.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tab?.let {
                    val position = it.position
                    val color = when (position) { 0 -> activeBuy; 1 -> activeSell; 2 -> activeHist; else -> inactive }
                    OrderUtils.setTabTextColor(context, it, color)
                    onTabSelectedAction(position) // Fragment의 handleTabSelection 호출
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                OrderUtils.setTabTextColor(context, tab, inactive)
            }
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    fun updateBuySellButtonAppearance(selectedPosition: Int) {
        val button = binding.buttonBuy ?: return
        
        // 로그인 상태 확인
        val isLoggedIn = PreferenceUtil.getMemberInfo() != null
        
        if (!isLoggedIn) {
            // 비로그인 상태일 때 로그인 버튼으로 표시
            button.text = "로그인"
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.main_point))
            button.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            button.isEnabled = true
            return
        }
        
        // 로그인 상태일 때 기존과 동일한 로직 유지
        when (selectedPosition) {
            0 -> {
                button.text = context.getString(R.string.button_buy)
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.percentage_positive_red))
                button.isEnabled = true
            }
            1 -> {
                button.text = context.getString(R.string.button_sell)
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.percentage_negative_blue))
                button.isEnabled = true
            }
            else -> {
                button.text = context.getString(R.string.button_buy)
                button.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                button.isEnabled = false
            }
        }
    }

    // 바인딩 참조를 위한 임시 코드 (실제 OrderUIInitializer 구현에 맞게 수정 필요)
    private val _binding: FragmentOrderContentBinding? = binding
}
package com.stip.ipasset.usd.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetUsdWithdrawalInputBinding
import com.stip.ipasset.fragment.BaseFragment
import com.stip.ipasset.usd.adapter.NumericKeypadAdapter
import com.stip.ipasset.usd.manager.USDAssetManager

/**
 * USD 출금 입력 화면
 */
class USDWithdrawalInputFragment : BaseFragment<FragmentIpAssetUsdWithdrawalInputBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetUsdWithdrawalInputBinding {
        return FragmentIpAssetUsdWithdrawalInputBinding.inflate(inflater, container, false)
    }

    // 현재 입력값 저장 변수
    private var currentInput = "1"
    
    // 소수점 입력 모드 추적
    private var decimalMode = false
    
    // 소수점 자리수 추적
    private var decimalDigits = 0
    
    // USD 자산 매니저
    private val assetManager = USDAssetManager.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 먼저 초기 데이터 포맷팅 시도
        formatWithdrawableAmount()
        
        // 출금가능 금액, 출금 한도 관찰 설정
        observeAssetData()
        
        // 뒤로 가기 버튼 설정
        binding.materialToolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 출금 가능 금액 정보 버튼 클릭 시 다이얼로그 표시
        binding.btnWithdrawableInfo.setOnClickListener {
            showWithdrawalAvailableInfoDialog()
        }

        // 출금 한도 정보 버튼 클릭 시 다이얼로그 표시
        binding.btnWithdrawalLimitInfo.setOnClickListener {
            showWithdrawalLimitInfoDialog()
        }

        // 키패드 설정
        setupKeypad()
        
        // 퍼센트 버튼 클릭 리스너 설정
        setupPercentageButtons()
        
        // 초기에는 키패드 숨김
        binding.rvKeypad.visibility = View.GONE
        
        // 출금금액 입력 컨테이너 클릭 시 키패드 표시
        binding.withdrawalInputContainer.setOnClickListener {
            // 키패드가 이미 표시되어 있으면 숨기고, 숨겨져 있으면 표시
            if (binding.rvKeypad.visibility == View.VISIBLE) {
                binding.rvKeypad.visibility = View.GONE
            } else {
                binding.rvKeypad.visibility = View.VISIBLE
                // 토스트 메시지 제거 - 사용자 요청에 따라
            }
        }

        // 출금 확인 버튼 텍스트 수정 및 클릭 이벤트 설정
        binding.btnWithdrawalApply.text = "출금신청"
        binding.btnWithdrawalApply.setOnClickListener {
            navigateToConfirmScreen()
        }
    }

    /**
     * 출금 가능 금액 정보 다이얼로그 표시
     */
    private fun showWithdrawalAvailableInfoDialog() {
        val dialogFragment = USDInfoDialogFragment()
        val args = Bundle()
        args.putString("dialog_layout", "dialog_ip_asset_usd_withdrawal_available_info")
        dialogFragment.arguments = args
        dialogFragment.show(childFragmentManager, "withdrawal_available_info_dialog")
    }

    /**
     * 출금 한도 정보 다이얼로그 표시
     */
    private fun showWithdrawalLimitInfoDialog() {
        val dialogFragment = USDInfoDialogFragment()
        val args = Bundle()
        args.putString("dialog_layout", "dialog_ip_asset_usd_withdrawal_limit_info")
        dialogFragment.arguments = args
        dialogFragment.show(childFragmentManager, "withdrawal_limit_info_dialog")
    }
    
    /**
     * 키패드 설정 및 처리 기능 구현
     */
    private fun setupKeypad() {
        val keypadAdapter = NumericKeypadAdapter { key ->
            when (key) {
                "<" -> deleteLastDigit()
                "완료" -> completeInput()
                else -> addDigit(key)
            }
        }
        
        // 키패드에 어댑터 설정
        binding.rvKeypad.adapter = keypadAdapter
    }
    
    /**
     * 숫자 추가
     */
    private fun addDigit(digit: String) {
        when (digit) {
            "." -> handleDecimalPoint()
            else -> handleNumericDigit(digit)
        }
        updateDisplay()
    }
    
    /**
     * 소수점 입력 처리
     */
    private fun handleDecimalPoint() {
        // 이미 소수점 모드면 무시
        if (decimalMode) return
        
        decimalMode = true
        decimalDigits = 0
        
        // 소수점 추가
        if (currentInput.isEmpty()) {
            currentInput = "0."
        } else {
            currentInput += "."
        }
    }
    
    /**
     * 숫자 입력 처리
     */
    private fun handleNumericDigit(digit: String) {
        // 초기 상태이면 초기화
        if (currentInput == "1" && !decimalMode) {
            currentInput = digit
            return
        }
        
        // 소수점 모드인 경우
        if (decimalMode) {
            // 소수점 이하 최대 2자리까지만 허용
            if (decimalDigits < 2) {
                currentInput += digit
                decimalDigits++
            }
        } else {
            // 일반 숫자 모드
            currentInput += digit
        }
    }
    
    /**
     * 마지막 숫자 삭제
     */
    private fun deleteLastDigit() {
        if (currentInput.isEmpty()) {
            return
        }
        
        // 콤마 제거
        val rawValue = currentInput.replace(",", "")
        
        // 마지막 문자가 소수점인지 확인
        if (rawValue.endsWith(".")) {
            decimalMode = false
            decimalDigits = 0
        } 
        // 소수점 이하 자리를 지울 경우
        else if (decimalMode && rawValue.contains(".")) {
            if (decimalDigits > 0) {
                decimalDigits--
            }
        }
        
        // 마지막 문자 삭제
        if (rawValue.length > 1) {
            currentInput = rawValue.substring(0, rawValue.length - 1)
        } else {
            // 하나의 숫자만 남았을 경우 "1"로 설정
            currentInput = "1"
            decimalMode = false
            decimalDigits = 0
        }
        
        // 숫자만 남았는데 모두 지워졌다면 "1"로 설정
        if (currentInput.isEmpty()) {
            currentInput = "1"
            decimalMode = false
            decimalDigits = 0
        }
        
        updateDisplay()
    }
    
    /**
     * 완료 버튼 처리
     */
    private fun completeInput() {
        // 키패드 숨기기
        binding.rvKeypad.visibility = View.GONE
        // 선택적: 입력 완료 피드백
        android.widget.Toast.makeText(requireContext(), "입력이 완료되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 퍼센트 버튼 설정
     */
    private fun setupPercentageButtons() {
        // 10% 버튼
        binding.btn10Percent.setOnClickListener {
            setPercentageAmount(0.1)
        }
        
        // 25% 버튼
        binding.btn25Percent.setOnClickListener {
            setPercentageAmount(0.25)
        }
        
        // 50% 버튼
        binding.btn50Percent.setOnClickListener {
            setPercentageAmount(0.5)
        }
        
        // MAX 버튼 - 수수료를 제외한 최대 출금 가능 금액으로 설정
        binding.btnMax.setOnClickListener {
            setMaxAmount()
        }
    }
    
    /**
     * 퍼센트에 해당하는 금액 설정
     */
    private fun setPercentageAmount(percentage: Double) {
        val withdrawableAmount = assetManager.withdrawableAmount.value ?: 10000.0
        val fee = assetManager.fee.value ?: 1.0
        
        // 출금 가능한 금액의 비율 계산 (수수료 고려)
        val maxAmount = withdrawableAmount - fee
        val percentAmount = maxAmount * percentage
        
        // 최소 금액 보장 (1 USD)
        val finalAmount = percentAmount.coerceAtLeast(1.0)
        
        // 소수점 자리를 포함한 금액으로 설정
        val formattedAmount = String.format("%.2f", finalAmount)
        currentInput = formattedAmount
        decimalMode = true
        decimalDigits = 2
        
        updateDisplay()
        
        android.util.Log.d("USDWithdrawal", "Selected ${percentage * 100}%: $finalAmount USD")
    }
    
    /**
     * 최대 출금 가능 금액 설정
     */
    private fun setMaxAmount() {
        val withdrawableAmount = assetManager.withdrawableAmount.value ?: 10000.0
        val fee = assetManager.fee.value ?: 1.0
        
        // 최대 가능 금액 (수수료 제외)
        val maxAmount = withdrawableAmount - fee
        
        // 소수점 자리를 포함한 최대 금액으로 설정
        val formattedAmount = String.format("%.2f", maxAmount)
        currentInput = formattedAmount
        decimalMode = true
        decimalDigits = 2
        
        updateDisplay()
        
        android.util.Log.d("USDWithdrawal", "Selected MAX: $maxAmount USD")
    }
    
    /**
     * 화면 표시 업데이트 - 입력 중인 금액을 있는 그대로 표시하면서 필요한 포맷팅만 적용
     */
    private fun updateDisplay() {
        try {
            var displayText: String
            var amount = 0.0
            
            // 1. 입력값 처리
            if (currentInput.isEmpty()) {
                displayText = "1"
                amount = 1.0
            } else {
                val cleanInput = currentInput.replace(",", "")
                
                // 마지막에 소수점만 있는 경우 (예: "123.")
                if (cleanInput.endsWith(".")) {
                    displayText = formatWholeNumber(cleanInput.dropLast(1)) + "."
                    amount = cleanInput.dropLast(1).toDoubleOrNull() ?: 1.0
                } 
                // 소수점 있는 완전한 수 (예: "123.45")
                else if (cleanInput.contains(".")) {
                    val parts = cleanInput.split(".")
                    val wholeNumber = formatWholeNumber(parts[0])
                    displayText = wholeNumber + "." + parts[1]
                    amount = cleanInput.toDoubleOrNull() ?: 1.0
                } 
                // 그냥 정수 (예: "123")
                else {
                    displayText = formatWholeNumber(cleanInput)
                    amount = cleanInput.toDoubleOrNull() ?: 1.0
                }
            }
            
            // 2. 최대 출금 가능 금액 체크
            val fee = assetManager.fee.value ?: 1.0
            val totalWithdrawable = assetManager.withdrawableAmount.value ?: 10000.0
            val maxAmount = totalWithdrawable - fee
            
            // 최대 금액 초과 시 제한
            if (amount > maxAmount) {
                val formatter = java.text.DecimalFormat("#,##0.00")
                displayText = formatter.format(maxAmount)
                currentInput = maxAmount.toString()
                decimalMode = true
                decimalDigits = 2
            }
            
            // 3. UI에 표시
            binding.withdrawalInput.text = displayText
            
            // 4. 수수료 및 총 출금액 업데이트 (최종 계산용으로는 완성된 숫자 사용)
            val finalAmount = if (decimalMode && decimalDigits == 0) {
                // 소수점만 입력된 경우 (예: "10.")
                amount
            } else if (decimalMode && decimalDigits < 2) {
                // 소수점 자리가 부족한 경우, 계산을 위해 0 추가
                val amountStr = currentInput + "0".repeat(2 - decimalDigits)
                amountStr.toDoubleOrNull() ?: amount
            } else {
                amount
            }
            
            updateFeeAndTotal(finalAmount)
            
            android.util.Log.d("USDWithdrawal", "Input: $currentInput, Display: $displayText, Amount: $finalAmount")
            
        } catch (e: Exception) {
            // 오류 발생 시 기본값으로 리셋
            binding.withdrawalInput.text = "1"
            currentInput = "1"
            decimalMode = false
            decimalDigits = 0
            android.util.Log.e("USDWithdrawal", "Error updating display: ${e.message}")
        }
    }
    
    /**
     * 정수 부분에만 천 단위 콤마 적용
     */
    private fun formatWholeNumber(number: String): String {
        if (number.isEmpty()) return "0"
        
        return try {
            val parsed = number.toLong()
            java.text.DecimalFormat("#,###").format(parsed)
        } catch (e: Exception) {
            number
        }
    }
    
    /**
     * 수수료 및 총 출금액 업데이트
     */
    private fun updateFeeAndTotal(amount: Double) {
        try {
            // 수수료 계산 (고정 1 USD로 가정)
            val fee = assetManager.fee.value ?: 1.0
            
            // 총 출금액 계산
            val total = amount + fee
            
            // 포맷터
            val formatter = java.text.DecimalFormat("#,##0.00")
            
            // UI 업데이트
            binding.feeAmount.text = "${formatter.format(fee)} USD"
            binding.totalWithdrawalAmount.text = formatter.format(total)
        } catch (e: Exception) {
            android.util.Log.e("USDWithdrawal", "Error updating fee and total", e)
        }
    }
    
    /**
     * 출금 확인 화면으로 이동
     */
    private fun navigateToConfirmScreen() {
        try {
            // 입력된 출금 금액 가져오기
            val withdrawalAmount = binding.withdrawalInput.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            
            // 최소 금액 확인
            if (withdrawalAmount < 1.0) {
                android.widget.Toast.makeText(requireContext(), "최소 출금 금액은 1 USD입니다.", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            
            // 수수료 가져오기
            val fee = assetManager.fee.value ?: 1.0
            
            // 출금 가능 금액 확인
            val withdrawable = assetManager.withdrawableAmount.value ?: 10000.0
            if (withdrawalAmount + fee > withdrawable) {
                android.widget.Toast.makeText(requireContext(), "출금 가능 금액을 초과했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            
            // 애니메이션 트랜지션 설정
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            
            fragmentTransaction.setCustomAnimations(
                com.stip.stip.R.anim.slide_in_right,
                com.stip.stip.R.anim.slide_out_left,
                com.stip.stip.R.anim.slide_in_left,
                com.stip.stip.R.anim.slide_out_right
            )
            
            // 확인 화면 프래그먼트 생성
            val confirmFragment = USDWithdrawalConfirmFragment()
            
            // 금액 정보 전달
            val args = Bundle()
            args.putDouble("withdrawal_amount", withdrawalAmount)
            args.putDouble("fee", fee)
            confirmFragment.arguments = args
            
            fragmentTransaction.replace(com.stip.stip.R.id.fragment_container, confirmFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            
            android.util.Log.d("USDWithdrawal", "Navigating to confirm screen with amount: $withdrawalAmount, fee: $fee")
            
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "처리 중 오류가 발생했습니다: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            android.util.Log.e("USDWithdrawal", "Error navigating to confirm screen", e)
        }
    }
    
    /**
     * USDAssetManager의 데이터 변화 관찰
     */
    private fun observeAssetData() {
        // 한 번만 formatter 인스턴스 생성
        val formatter = java.text.DecimalFormat("#,##0.00")
        
        // 출금 가능 금액 관찰
        assetManager.withdrawableAmount.observe(viewLifecycleOwner) { amount ->
            val formattedAmount = formatter.format(amount)
            binding.textWithdrawable.text = "$formattedAmount USD"
        }

        // 출금 한도 관찰
        assetManager.withdrawalLimit.observe(viewLifecycleOwner) { limit ->
            val formattedLimit = formatter.format(limit)
            binding.textLimit.text = "$formattedLimit USD"
        }
        
        // 출금 수수료 관찰
        assetManager.fee.observe(viewLifecycleOwner) { fee ->
            val formattedFee = formatter.format(fee)
            binding.feeAmount.text = "$formattedFee USD"
        }
    }
    
    /**
     * 출금가능 금액을 소수점 2자리로 포맷팅 (초기값 설정용)
     */
    private fun formatWithdrawableAmount() {
        // 명시적으로 USDAssetManager에 데이터 리프레시 요청
        assetManager.refreshData()
        
        // 현재 값을 즉시 반영 (LiveData 업데이트 전에)
        val formatter = java.text.DecimalFormat("#,##0.00")
        
        // 기본값을 사용하여 UI 초기화 - LiveData 변경 전에 표시되는 값
        val withdrawableAmount = assetManager.withdrawableAmount.value ?: 10000.0
        val formattedWithdrawable = formatter.format(withdrawableAmount)
        binding.textWithdrawable.text = "$formattedWithdrawable USD"
        
        android.util.Log.d("USDWithdrawalInputFragment", "Initial withdrawable amount: $formattedWithdrawable USD")
    }
}

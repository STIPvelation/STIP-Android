package com.stip.ipasset.ticker.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetTickerWithdrawalInputBinding
import com.stip.stip.ipasset.fragment.BaseFragment
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.ipasset.WithdrawalInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

/**
 * 티커 출금 입력을 처리하는 Fragment
 * 출금 금액과 주소를 입력받습니다.
 */
@AndroidEntryPoint
class TickerWithdrawalInputFragment : BaseFragment<FragmentIpAssetTickerWithdrawalInputBinding>() {
    private val args by navArgs<com.stip.ipasset.ticker.fragment.TickerWithdrawalInputFragmentArgs>()
    private val ipAsset: IpAsset get() = args.ipAsset
    private val currencyCode: String get() = ipAsset.currencyCode

    private val viewModel by viewModels<WithdrawalInputViewModel>()
    
    // 출금 가능한 최대 금액 (실제 앱에서는 API 응답에서 가져올 것)
    private val availableAmount = 40.0
    private val maxAmount = 1000000.0
    private val fee = 1.0
    
    private fun bind() = with(viewBinding) {
        // 툴바 설정
        materialToolbar.title = "$currencyCode ${getString(R.string.common_withdrawal_action)}"
        materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        // 가용 잔액 및 한도 설정
        tvAvailableAmount.text = "$availableAmount $currencyCode"
        tvMaxAmount.text = NumberFormat.getNumberInstance(Locale.getDefault()).format(maxAmount) + " $currencyCode"
        
        // 수수료 정보 표시
        tvFeeAmount.text = "$fee $currencyCode"
        tvTotalFeeAmount.text = "$fee $currencyCode" // 총 수수료는 일단 동일하게 설정
        
        // 퍼센트 버튼 리스너 설정
        setupPercentageButtons()
        
        // 금액 입력 TextWatcher 설정
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 금액이 변경될 때 수행할 로직 (검증 등)
            }
        })

        // 출금신청 버튼 설정
        btnContinue.setOnClickListener {
            if (validateInputs()) {
                navigateToConfirmation()
            }
        }
    }
    
    private fun setupPercentageButtons() = with(viewBinding) {
        btnPercent10.setOnClickListener { calculateAmountByPercentage(0.1) }
        btnPercent25.setOnClickListener { calculateAmountByPercentage(0.25) }
        btnPercent50.setOnClickListener { calculateAmountByPercentage(0.5) }
        btnPercent100.setOnClickListener { calculateAmountByPercentage(1.0) }
    }
    
    private fun calculateAmountByPercentage(percentage: Double) = with(viewBinding) {
        val calculatedAmount = availableAmount * percentage
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", calculatedAmount))
    }
    
    private fun validateInputs(): Boolean = with(viewBinding) {
        // 금액 검증
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(context, "금액을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "유효한 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (amount < 1.0) {
            Toast.makeText(context, "\ucd5c\uc18c \ucd9c\uae08 \uae08\uc561\uc740 1 ${currencyCode} \uc785\ub2c8\ub2e4", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (amount > availableAmount) {
            Toast.makeText(context, "출금 가능 금액을 초과했습니다", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // 주소 검증
        val address = etAddress.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(context, "출금 주소를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun navigateToConfirmation() {
        // 입력한 데이터를 NavDirections를 통해 확인 화면으로 이동
        val withdrawalAmount = viewBinding.etAmount.text.toString().toFloatOrNull() ?: 0.0f
        val withdrawalAddress = viewBinding.etAddress.text.toString().trim()
        
        val action = TickerWithdrawalInputFragmentDirections
            .actionTickerWithdrawalInputFragmentToTickerWithdrawalConfirmFragment(
                ipAsset,
                withdrawalAmount,
                fee.toFloat(),
                withdrawalAddress
            )
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalInputBinding {
        return FragmentIpAssetTickerWithdrawalInputBinding.inflate(inflater, container, false)
    }
}

package com.stip.ipasset.ticker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.stip.ipasset.fragment.BaseFragment
import com.stip.ipasset.model.IpAsset
import com.stip.ipasset.ticker.viewmodel.WithdrawalConfirmViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetTickerWithdrawalConfirmBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat

/**
 * 티커 출금 정보 확인 Fragment
 * Navigation Component에 의존하지 않는 버전
 */
@AndroidEntryPoint
class TickerWithdrawalConfirmFragmentCompat : BaseFragment<FragmentIpAssetTickerWithdrawalConfirmBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalConfirmBinding {
        return FragmentIpAssetTickerWithdrawalConfirmBinding.inflate(inflater, container, false)
    }
    
    // Bundle에서 직접 인자 가져오기
    private lateinit var ipAsset: IpAsset
    private var amount: Float = 0.0f
    private var fee: Float = 0.0f
    private var address: String = ""
    private val currencyCode: String get() = ipAsset.currencyCode
    
    // ViewModel 생성
    private val viewModel by viewModels<WithdrawalConfirmViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bundle에서 인자 추출
        arguments?.let { args ->
            args.getParcelable<IpAsset>(ARG_IP_ASSET)?.let { asset ->
                ipAsset = asset
            }
            amount = args.getFloat(ARG_AMOUNT, 0.0f)
            fee = args.getFloat(ARG_FEE, 0.0f)
            address = args.getString(ARG_ADDRESS, "")
        }
    }
    
    private fun bind() {
        val binding = binding ?: return
        with(binding) {
            // 툴바 설정
            materialToolbar.title = "$currencyCode ${getString(R.string.common_withdrawal_action)}"
            materialToolbar.setNavigationOnClickListener {
                // NavController 대신 FragmentManager 사용
                parentFragmentManager.popBackStack()
            }
            
            // 출금 상세 정보 표시
            val decimalFormat = DecimalFormat("#,##0.00")
            
            // XML 레이아웃의 ID와 일치하도록 변경
            tvAmountValue.text = "${decimalFormat.format(amount.toDouble())} $currencyCode"
            tvFeeValue.text = "${decimalFormat.format(fee.toDouble())} $currencyCode"
            tvAddressValue.text = address
            tvTickerValue.text = currencyCode
            
            // 총 출금액 (출금액 + 수수료)
            val total = amount + fee
            tvTotalAmountValue.text = "${decimalFormat.format(total.toDouble())} $currencyCode"
            
            // 출금 신청 버튼 (XML의 btnConfirmWithdrawal와 일치)
            btnConfirmWithdrawal.setOnClickListener {
                processWithdrawal()
            }
        }
    }
    
    private fun processWithdrawal() {
        // 출금 처리 로직
        showLoadingIndicator(true)
        
        // 출금 진행 로직 (실제로는 API 호출 등이 여기에 구현됨)
        // 여기서는 간단하게 2초 딜레이 후 성공으로 처리
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showLoadingIndicator(false)
            
            // 출금 신청 완료 후 상세 화면으로 이동
            navigateToWithdrawalDetail()
        }, 2000)
    }
    
    private fun showLoadingIndicator(isLoading: Boolean) {
        val binding = binding ?: return
        with(binding) {
            // 프로그레스 바가 XML에 없어서 임시로 처리
            if (isLoading) {
                btnConfirmWithdrawal.isEnabled = false
                btnConfirmWithdrawal.text = "처리 중..."
            } else {
                btnConfirmWithdrawal.isEnabled = true
                btnConfirmWithdrawal.text = getString(R.string.common_withdrawal_action)
            }
        }
    }
    
    private fun navigateToWithdrawalDetail() {
        Toast.makeText(requireContext(), "출금 신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        
        // 메인 화면으로 이동 (모든 백스택 제거)
        parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }
    
    companion object {
        const val ARG_IP_ASSET = "arg_ip_asset"
        const val ARG_AMOUNT = "arg_amount"
        const val ARG_FEE = "arg_fee"
        const val ARG_ADDRESS = "arg_address"
        
        // 새 인스턴스 생성 헬퍼 메서드
        fun newInstance(
            ipAsset: IpAsset,
            amount: Float,
            fee: Float,
            address: String
        ): TickerWithdrawalConfirmFragmentCompat {
            return TickerWithdrawalConfirmFragmentCompat().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_IP_ASSET, ipAsset)
                    putFloat(ARG_AMOUNT, amount)
                    putFloat(ARG_FEE, fee)
                    putString(ARG_ADDRESS, address)
                }
            }
        }
    }
}

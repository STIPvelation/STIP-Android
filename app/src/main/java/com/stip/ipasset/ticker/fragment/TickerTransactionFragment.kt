package com.stip.ipasset.ticker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stip.dummy.AssetDummyData
import com.stip.ipasset.model.IpAsset
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetTickerTransactionBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class TickerTransactionFragment : Fragment() {

    private var _binding: FragmentIpAssetTickerTransactionBinding? = null
    private val binding get() = _binding!!
    
    private var tickerCode: String? = null
    private var amount: Double = 0.0
    private var usdEquivalent: Double = 0.0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전달된 데이터 추출
        arguments?.let {
            tickerCode = it.getString(ARG_TICKER_CODE)
            amount = it.getDouble(ARG_AMOUNT, 0.0)
            usdEquivalent = it.getDouble(ARG_USD_EQUIVALENT, 0.0)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpAssetTickerTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 뒤로가기 버튼 설정
        binding.materialToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        
        // 티커 정보 표시
        setupTickerInfo()
        
        // 버튼 리스너 설정
        setupButtonListeners()
    }
    
    private fun setupTickerInfo() {
        tickerCode?.let { code ->
            // 티커 데이터 가져오기
            val asset = AssetDummyData.getAssetByCode(code) ?: return
            
            // 툴바 타이틀 설정
            binding.materialToolbar.title = "${code} 총 보유"
            
            // 티커 로고 설정
            val tickerInitials = code.take(2)
            binding.currencyIconText.text = tickerInitials
            
            // 티커 색상 설정
            val colorResId = when(code) {
                "JWV" -> R.color.token_jwv
                "MDM" -> R.color.token_mdm
                "CDM" -> R.color.token_cdm
                "IJECT" -> R.color.token_iject
                "WETALK" -> R.color.token_wetalk
                "SLEEP" -> R.color.token_sleep
                "KCOT" -> R.color.token_kcot
                "MSK" -> R.color.token_msk
                "SMT" -> R.color.token_smt
                "AXNO" -> R.color.token_axno
                "KATV" -> R.color.token_katv
                else -> R.color.token_usd
            }
            binding.currencyIconBackground.backgroundTintList = requireContext().getColorStateList(colorResId)
            
            // 잔액 설정 (ticker_holdings 사용)
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            binding.tickerHoldings.text = "${formatter.format(amount)} ${code}"
            
            // USD 환산 가치 설정 (equivalent_amount 사용)
            binding.equivalentAmount.text = "≈ $${formatter.format(usdEquivalent)}"
        }
    }
    
    private fun setupButtonListeners() {
        // 입금 버튼 (deposit_button_container 사용)
        binding.depositButtonContainer.setOnClickListener {
            // 입금 화면으로 이동 로직 추가
            navigateToTickerDepositScreen()
        }
        
        // 출금 버튼 (withdraw_button_container 사용)
        binding.withdrawButtonContainer.setOnClickListener {
            // 출금 화면으로 이동 로직 추가
            navigateToTickerWithdrawalScreen()
        }
    }
    
    private fun navigateToTickerDepositScreen() {
        tickerCode?.let { code ->
            try {
                // fragment 인스턴스를 사용
                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                
                // 애니메이션 추가
                fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                
                // 티커 입금 프래그먼트로 이동
                val tickerDepositFragment = TickerDepositFragment()
                
                // AssetDummyData에서 데이터 가져오기
                val asset = AssetDummyData.getAssetByCode(code)
                
                if (asset != null) {
                    // 번들에 데이터 전달
                    val bundle = Bundle()
                    bundle.putParcelable("ipAsset", asset)
                    tickerDepositFragment.arguments = bundle
                    
                    // 프래그먼트 교체 및 백스택에 추가
                    fragmentTransaction.replace(R.id.fragment_container, tickerDepositFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                } else {
                    // 자산 데이터를 찾지 못한 경우 오류 메시지 표시
                    android.widget.Toast.makeText(requireContext(), "${code} 데이터를 찾을 수 없습니다", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), "화면 전환 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            android.widget.Toast.makeText(requireContext(), "티커 정보가 없습니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToTickerWithdrawalScreen() {
        // 출금 화면으로 이동하는 로직 구현
        // 더미 구현: 토스트 메시지로 기능 알림
        android.widget.Toast.makeText(requireContext(), "${tickerCode} 출금 기능 준비 중", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val ARG_TICKER_CODE = "tickerCode"
        private const val ARG_AMOUNT = "amount"
        private const val ARG_USD_EQUIVALENT = "usdEquivalent"
        
        fun newInstance(tickerCode: String, amount: Double, usdEquivalent: Double): TickerTransactionFragment {
            val fragment = TickerTransactionFragment()
            val args = Bundle().apply {
                putString(ARG_TICKER_CODE, tickerCode)
                putDouble(ARG_AMOUNT, amount)
                putDouble(ARG_USD_EQUIVALENT, usdEquivalent)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

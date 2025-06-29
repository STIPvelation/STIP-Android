package com.stip.ipasset.ticker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.stip.dummy.AssetDummyData
import com.stip.dummy.TickerTransactionDummyData
import com.stip.ipasset.model.IpAsset
import com.stip.ipasset.ticker.activity.TickerDepositDetailActivity
import com.stip.ipasset.ticker.activity.TickerWithdrawalDetailActivity
import com.stip.ipasset.ticker.adapter.TickerTransactionAdapter
import com.stip.ipasset.ticker.model.TickerDepositTransaction
import com.stip.ipasset.ticker.model.TickerWithdrawalTransaction
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
    
    private lateinit var transactionAdapter: TickerTransactionAdapter
    private var currentFilter: TransactionFilter = TransactionFilter.ALL
    
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
        
        // 어댑터 초기화 및 설정
        setupTransactionAdapter()
        
        // 필터 탭 설정
        setupFilterTabs()
        
        // 버튼 리스너 설정
        setupButtonListeners()
    }
    
    private fun setupTickerInfo() {
        tickerCode?.let { code ->
            // 티커 데이터 가져오기
            val asset = AssetDummyData.getAssetByCode(code) ?: return
            
            // 툴바 타이틀 설정
            binding.materialToolbar.title = "총 보유"
            
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
    
    // 거래 내역 어댑터 설정
    private fun setupTransactionAdapter() {
        // 어댑터 초기화
        transactionAdapter = TickerTransactionAdapter { transaction ->
            when (transaction) {
                is TickerDepositTransaction -> {
                    // 입금 내역 상세 화면으로 이동
                    val intent = Intent(requireContext(), TickerDepositDetailActivity::class.java).apply {
                        putExtra("tickerSymbol", transaction.tickerSymbol)
                        putExtra("tickerAmount", transaction.tickerAmount)
                        putExtra("usdAmount", transaction.usdAmount)
                        putExtra("timestamp", transaction.timestamp)
                        putExtra("status", transaction.status)
                        putExtra("txHash", transaction.txHash)
                    }
                    startActivity(intent)
                }
                is TickerWithdrawalTransaction -> {
                    // 출금 내역 상세 화면으로 이동
                    val intent = Intent(requireContext(), TickerWithdrawalDetailActivity::class.java).apply {
                        putExtra("tickerSymbol", transaction.tickerSymbol)
                        putExtra("tickerAmount", transaction.tickerAmount)
                        putExtra("usdAmount", transaction.usdAmount)
                        putExtra("timestamp", transaction.timestamp)
                        putExtra("status", transaction.status)
                        putExtra("txHash", transaction.txHash)
                        putExtra("recipientAddress", transaction.recipientAddress)
                        putExtra("fee", transaction.fee)
                    }
                    startActivity(intent)
                }
            }
        }

        // 리사이클러뷰 설정
        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        // 초기 데이터 로드
        loadTransactionData()
    }

    // 필터 탭 설정
    private fun setupFilterTabs() {
        // 전체 탭
        binding.tabAll.setOnClickListener {
            currentFilter = TransactionFilter.ALL
            updateTabSelection()
            loadTransactionData()
        }

        // 입금 탭
        binding.tabDeposit.setOnClickListener {
            currentFilter = TransactionFilter.DEPOSIT
            updateTabSelection()
            loadTransactionData()
        }

        // 출금 탭
        binding.tabWithdrawal.setOnClickListener {
            currentFilter = TransactionFilter.WITHDRAWAL
            updateTabSelection()
            loadTransactionData()
        }

        // 초기 탭 선택 상태 설정
        updateTabSelection()
    }

    // 탭 선택 상태 업데이트
    private fun updateTabSelection() {
        // 모든 탭을 비선택 상태로 초기화
        binding.tabAll.setBackgroundResource(R.drawable.bg_rounded_transparent_button)
        binding.tabAll.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        
        binding.tabDeposit.setBackgroundResource(R.drawable.bg_rounded_transparent_button)
        binding.tabDeposit.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        
        binding.tabWithdrawal.setBackgroundResource(R.drawable.bg_rounded_transparent_button)
        binding.tabWithdrawal.setTextColor(resources.getColor(android.R.color.darker_gray, null))

        // 선택된 탭 강조
        when (currentFilter) {
            TransactionFilter.ALL -> {
                binding.tabAll.setBackgroundResource(R.drawable.bg_rounded_blue_button)
                binding.tabAll.setTextColor(resources.getColor(android.R.color.white, null))
            }
            TransactionFilter.DEPOSIT -> {
                binding.tabDeposit.setBackgroundResource(R.drawable.bg_rounded_blue_button)
                binding.tabDeposit.setTextColor(resources.getColor(android.R.color.white, null))
            }
            TransactionFilter.WITHDRAWAL -> {
                binding.tabWithdrawal.setBackgroundResource(R.drawable.bg_rounded_blue_button)
                binding.tabWithdrawal.setTextColor(resources.getColor(android.R.color.white, null))
            }
        }
    }

    // 트랜잭션 데이터 로드 및 필터링
    private fun loadTransactionData() {
        // 티커코드가 없으면 빈 리스트 표시
        val tickerCode = this.tickerCode ?: return
        
        val transactions = when (currentFilter) {
            TransactionFilter.ALL -> {
                TickerTransactionDummyData.getAllTransactions(tickerCode)
            }
            TransactionFilter.DEPOSIT -> {
                TickerTransactionDummyData.getTickerDepositTransactions(tickerCode)
            }
            TransactionFilter.WITHDRAWAL -> {
                TickerTransactionDummyData.getTickerWithdrawalTransactions(tickerCode)
            }
        }

        // 어댑터에 데이터 설정
        transactionAdapter.submitList(transactions)
        
        // 빈 상태 처리
        if (transactions.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.recyclerViewTransactions.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.recyclerViewTransactions.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // 트랜잭션 필터 열거형
    enum class TransactionFilter {
        ALL,       // 전체
        DEPOSIT,   // 입금
        WITHDRAWAL  // 출금
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

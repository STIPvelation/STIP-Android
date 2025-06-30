package com.stip.ipasset.ticker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
// Removed dummy data imports
import com.stip.ipasset.model.IpAsset
import com.stip.ipasset.ticker.activity.TickerDepositDetailActivity
import com.stip.ipasset.ticker.activity.TickerWithdrawalDetailActivity
import com.stip.ipasset.ticker.adapter.TickerTransactionAdapter
import com.stip.ipasset.ticker.fragment.TickerWithdrawalInputFragmentCompat
import com.stip.ipasset.ticker.model.TickerDepositTransaction
import com.stip.ipasset.ticker.model.TickerWithdrawalTransaction
import com.stip.stip.MainActivity
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
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
    
    override fun onResume() {
        super.onResume()
        // 상세 화면에서 돌아올 때 타이틀을 '총 보유'로 유지
        binding.materialToolbar.title = "총 보유"
        // 타이틀 텍스트 설정이 다른 곳에서 변경되지 않도록 post 사용
        binding.materialToolbar.post {
            binding.materialToolbar.title = "총 보유"
        }
        
        // MainActivity의 헤더 완전히 숨기기
        (activity as? MainActivity)?.setHeaderVisibility(false)
    }
    
    override fun onStop() {
        super.onStop()
    }
    
    private fun setupTickerInfo() {
        tickerCode?.let { code ->
            // API로 대체 예정
            // 임시 데이터 사용
            val asset = IpAsset(id = code, name = code, ticker = code, balance = amount, value = usdEquivalent)
            
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
                
                // 임시 자산 객체 생성 (API로 대체 예정)
                val asset = IpAsset(id = code, name = code, ticker = code, balance = amount, value = usdEquivalent)
                
                // 번들에 데이터 전달
                val bundle = Bundle()
                // Android API 33+ 호환성 위해 타입 명시
                bundle.putParcelable("ipAsset", asset as android.os.Parcelable)
                tickerDepositFragment.arguments = bundle
                
                // 프래그먼트 교체 및 백스택에 추가
                fragmentTransaction.replace(R.id.fragment_container, tickerDepositFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), "화면 전환 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            android.widget.Toast.makeText(requireContext(), "티커 정보가 없습니다", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToTickerWithdrawalScreen() {
        tickerCode?.let { code ->
            // 임시 자산 객체 생성 (API로 대체 예정)
            val asset = IpAsset(id = code, name = code, ticker = code, balance = amount, value = usdEquivalent)
            
            try {
                // NavController를 사용하지 않는 TickerWithdrawalInputFragmentCompat 인스턴스 생성
                val withdrawalFragment = TickerWithdrawalInputFragmentCompat.newInstance(asset)
                
                // Fragment Transaction으로 화면 교체
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, withdrawalFragment)
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                android.util.Log.e("TickerTransaction", "출금 화면 이동 실패: ${e.message}", e)
                android.widget.Toast.makeText(
                    requireContext(),
                    "화면 전환 중 오류가 발생했습니다: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } ?: run {
            // 티커 코드가 없는 경우
            android.widget.Toast.makeText(
                requireContext(),
                "티커 정보가 없습니다.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
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
        
        // API 호출로 대체 예정
        val transactions: List<Any> = when (currentFilter) {
            TransactionFilter.ALL -> {
                listOf<Any>() // 명시적 타입 지정
                // API 호출: getAllTransactions(tickerCode)
            }
            TransactionFilter.DEPOSIT -> {
                listOf<TickerDepositTransaction>() // 명시적 타입 지정
                // API 호출: getDepositTransactions(tickerCode)
            }
            TransactionFilter.WITHDRAWAL -> {
                listOf<TickerWithdrawalTransaction>() // 명시적 타입 지정
                // API 호출: getWithdrawalTransactions(tickerCode)
            }
        }

        // 어댑터에 데이터 설정
        transactionAdapter.submitList(transactions)
        
        // 빈 상태 처리
        if (transactions.isEmpty()) { // 명시적 타입 지정으로 오버로드 해결
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

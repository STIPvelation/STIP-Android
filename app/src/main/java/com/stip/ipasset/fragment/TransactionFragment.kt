package com.stip.stip.ipasset.fragment

import androidx.core.content.ContextCompat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import com.stip.stip.R
import com.stip.stip.databinding.FragmentTransactionBinding
import com.stip.stip.ipasset.TransactionViewModel
import com.stip.ipasset.ticker.adapter.TickerTransactionHistoryAdapter
import com.stip.ipasset.usd.adapter.UsdTransactionHistoryAdapter

import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.ipasset.model.TransactionHistory
import com.stip.stip.iphome.fragment.InfoDialogFragment
import com.stip.stip.ipasset.model.WithdrawalStatus
import com.stip.ipasset.usd.activity.DepositKrwActivity
import com.stip.ipasset.usd.activity.UsdDepositDetailActivity
import com.stip.ipasset.usd.activity.UsdWithdrawalDetailActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log


class TransactionFragment : com.stip.stip.ipasset.fragment.BaseFragment<FragmentTransactionBinding>() {

    private val navArgs: TransactionFragmentArgs by navArgs()
    private val ipAsset: IpAsset get() = navArgs.ipAsset

    private val viewModel by viewModels<TransactionViewModel>()
    
    // USD 전용 어댑터
    private val usdAdapter by lazy {
        UsdTransactionHistoryAdapter { transaction ->
            showTransactionDetail(transaction)
        }
    }
    
    // 티커 전용 어댑터
    private val tickerAdapter by lazy {
        TickerTransactionHistoryAdapter { transaction ->
            showTransactionDetail(transaction)
        }
    }

    private var currentFilter = com.stip.stip.ipasset.model.Filter.ALL

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionBinding {
        return FragmentTransactionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupFilterTabs()
        setupClickListeners()
        setupRecyclerView()
        showSampleTransactionHistory() // 샘플 데이터도 유지
        collectData()

        // 거래 내역 불러오기
        lifecycleScope.launch {
            viewModel.fetchTransactionHistory(ipAsset)
        }

        // 출금 계좌 번호 (USD 기준, 다국어 적용)
        val accountNumber = getString(R.string.account_number2)

        // 출금 상태 정보 로딩
        lifecycleScope.launch {
            viewModel.fetchWithdrawalStatus(ipAsset, accountNumber)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setupToolbar() = with(viewBinding.materialToolbar) {
        setNavigationOnClickListener { activity?.finish() }
    }

    private fun setupFilterTabs() {
        viewBinding.tabAll.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.ALL)
            applyNewFilter(com.stip.stip.ipasset.model.Filter.ALL)
        }
        
        viewBinding.tabDeposit.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.DEPOSIT)
            applyNewFilter(com.stip.stip.ipasset.model.Filter.DEPOSIT)
        }
        
        viewBinding.tabWithdrawal.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.WITHDRAW)
            applyNewFilter(com.stip.stip.ipasset.model.Filter.WITHDRAW)
        }
        
        viewBinding.tabReturn.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.REFUND)
            applyNewFilter(com.stip.stip.ipasset.model.Filter.REFUND)
        }
        
        viewBinding.tabInProgress.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.PROCESSING)
            applyNewFilter(com.stip.stip.ipasset.model.Filter.PROCESSING)
        }
        
        // 초기 탭 상태 설정 (전체 선택)
        updateActiveTab(com.stip.stip.ipasset.model.Filter.ALL)
        applyNewFilter(com.stip.stip.ipasset.model.Filter.ALL)
    }
    
    /**
     * 새 필터를 적용하는 메서드
     * ViewModel에 필터를 적용하고 UI를 업데이트합니다.
     */
    private fun applyNewFilter(filter: com.stip.stip.ipasset.model.Filter) {
        // 현재 필터 업데이트
        currentFilter = filter
        
        // ViewModel에 필터 적용
        viewModel.applyFilter(filter)
        
        // UI 업데이트 (샘플 트랜잭션 아이템 필터링)
        filterTransactionsBasedOnCurrentFilter()
    }
    
    private fun updateActiveTab(filter: com.stip.stip.ipasset.model.Filter) {
        // 모든 탭 비활성화
        viewBinding.tabAll.setBackgroundResource(android.R.color.transparent)
        viewBinding.tabAll.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        
        viewBinding.tabDeposit.setBackgroundResource(android.R.color.transparent)
        viewBinding.tabDeposit.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        
        viewBinding.tabWithdrawal.setBackgroundResource(android.R.color.transparent)
        viewBinding.tabWithdrawal.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        
        viewBinding.tabReturn.setBackgroundResource(android.R.color.transparent)
        viewBinding.tabReturn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        
        viewBinding.tabInProgress.setBackgroundResource(android.R.color.transparent)
        viewBinding.tabInProgress.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        
        // 선택된 탭 활성화
        when (filter) {
            com.stip.stip.ipasset.model.Filter.ALL -> {
                viewBinding.tabAll.setBackgroundResource(R.drawable.button_rounded_background)
                viewBinding.tabAll.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            com.stip.stip.ipasset.model.Filter.DEPOSIT -> {
                viewBinding.tabDeposit.setBackgroundResource(R.drawable.button_rounded_background)
                viewBinding.tabDeposit.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            com.stip.stip.ipasset.model.Filter.WITHDRAW -> {
                viewBinding.tabWithdrawal.setBackgroundResource(R.drawable.button_rounded_background)
                viewBinding.tabWithdrawal.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            com.stip.stip.ipasset.model.Filter.REFUND -> {
                viewBinding.tabReturn.setBackgroundResource(R.drawable.button_rounded_background)
                viewBinding.tabReturn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            com.stip.stip.ipasset.model.Filter.PROCESSING -> {
                viewBinding.tabInProgress.setBackgroundResource(R.drawable.button_rounded_background)
                viewBinding.tabInProgress.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            else -> {}
        }
    }

    private fun setupClickListeners() {
        viewBinding.depositButtonContainer.setOnClickListener {
            // 통화 코드에 따라 다른 입금 화면으로 이동
            if (ipAsset.currencyCode == "USD") {
                // USD 입금은 DepositKrwActivity로 이동
                val intent = Intent(requireContext(), DepositKrwActivity::class.java)
                startActivity(intent)
            } else {
                // 티커 입금은 ticker deposit fragment로 이동
                try {
                    val directions = TransactionFragmentDirections.actionIpAssetHoldingsFragmentToTickerDepositFragment(ipAsset)
                    findNavController().navigate(directions)
                    Log.d("TransactionFragment", "티커 입금 페이지로 이동: ${ipAsset.currencyCode}")
                } catch (e: Exception) {
                    Log.e("TransactionFragment", "티커 입금 페이지 이동 오류", e)
                    Toast.makeText(requireContext(), "티커 입금 페이지 이동 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Set click listeners for sample transaction items to display a Toast message
        viewBinding.sampleItemDeposit.setOnClickListener {
            // Show a toast with transaction details instead of launching an activity
            Toast.makeText(
                requireContext(),
                "Sample Deposit: 5,000.00 USD (≈ 6,500,000 KRW)",
                Toast.LENGTH_SHORT
            ).show()
            
            // Update list item formatting
            try {
                viewBinding.sampleItemDeposit.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 5000.0)
                }
                viewBinding.sampleItemDeposit.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.0f KRW", 6500000.0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewBinding.sampleItemWithdraw.setOnClickListener {
            // Show a toast with transaction details instead of launching an activity
            Toast.makeText(
                requireContext(),
                "Sample Withdrawal: 10,000.00 USD (≈ 13,000,000 KRW)",
                Toast.LENGTH_SHORT
            ).show()
            
            // Update list item formatting
            try {
                viewBinding.sampleItemWithdraw.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 10000.0)
                }
                viewBinding.sampleItemWithdraw.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.0f KRW", 13000000.0) // 출금 예시를 위한 KRW 값
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewBinding.sampleItemDeposit2.setOnClickListener {
            // Show a toast with transaction details instead of launching an activity
            Toast.makeText(
                requireContext(),
                "Sample Deposit: 2,500.00 USD (≈ 3,250,000 KRW)",
                Toast.LENGTH_SHORT
            ).show()
            
            // Update list item formatting
            try {
                viewBinding.sampleItemDeposit2.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 2500.0)
                }
                viewBinding.sampleItemDeposit2.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.0f KRW", 3250000.0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewBinding.withdrawButtonContainer.setOnClickListener {
            if (ipAsset.currencyCode == "USD") {
                navigateToWithdraw()
            } else {
                showWithdrawConfirmDialog()
            }
        }

        // 제거된 withdrawable 및 textWithdrawalLimit 관련 코드
    }

    private fun showDepositConfirmDialog() {
        InfoDialogFragment.newInstance(
            titleResId = R.string.deposit_warning,
            message = getString(R.string.deposit_network_warning),
            titleColorResId = R.color.dialog_title_buy_error_red
        ).apply {
            setConfirmClickListener {
                // Start DepositKrwActivity on confirmation
                val intent = Intent(requireContext(), DepositKrwActivity::class.java)
                startActivity(intent)
            }
        }.show(parentFragmentManager, InfoDialogFragment.TAG)
    }

    private fun showWithdrawConfirmDialog() {
        InfoDialogFragment.newInstance(
            titleResId = R.string.withdraw_warning,
            message = getString(R.string.withdraw_network_warning),
            titleColorResId = R.color.dialog_title_info_blue
        ).apply {
            setConfirmClickListener {
                navigateToWithdraw()
            }
        }.show(parentFragmentManager, InfoDialogFragment.TAG)
    }

    private fun navigateToDeposit() {
        try {
            // Start DepositKrwActivity instead of navigating to a fragment
            val intent = Intent(requireContext(), DepositKrwActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("TransactionFragment", "Deposit Navigation Error", e)
            Toast.makeText(requireContext(), getString(R.string.deposit_navigation_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToWithdraw() {
        try {
            if (ipAsset.currencyCode == "USD") {
                // USD withdrawal flow
                val currentWithdrawalStatus = viewModel.withdrawalStatus.value
                if (currentWithdrawalStatus != null) {
                    val directions = TransactionFragmentDirections
                        .actionIpAssetHoldingsFragmentToWithdrawFragment(currentWithdrawalStatus as WithdrawalStatus)
                    findNavController().navigate(directions)
                } else {
                    Log.w("TransactionFragment", "Withdrawal status is null, cannot navigate to USD withdrawal.")
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.withdraw_status_null_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Ticker withdrawal flow - navigate directly to ticker withdrawal input fragment
                // Use the fragment ID directly with a bundle containing the ipAsset
                val bundle = Bundle().apply {
                    putParcelable("ipAsset", ipAsset)
                }
                findNavController().navigate(R.id.tickerWithdrawalInputFragment, bundle)
            }
        } catch (e: Exception) {
            Log.e("TransactionFragment", "Withdraw Navigation Error", e)
            Toast.makeText(
                requireContext(),
                getString(R.string.withdraw_navigation_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 샘플 트랜잭션 히스토리 보이기
    private fun showSampleTransactionHistory() {
        // 현재 필터에 따라 샘플 아이템 필터링
        filterTransactionsBasedOnCurrentFilter()
    }
    
    private fun filterTransactionsBasedOnCurrentFilter() {
        // 처음에 모든 아이템을 숨김
        viewBinding.sampleItemDeposit.visibility = View.GONE
        viewBinding.sampleItemWithdraw.visibility = View.GONE
        viewBinding.sampleItemDeposit2.visibility = View.GONE
        
        // 현재 선택된 필터에 맞는 아이템만 표시
        when(currentFilter) {
            com.stip.stip.ipasset.model.Filter.ALL -> {
                // 전체: 모든 트랜잭션 표시
                viewBinding.sampleItemDeposit.visibility = View.VISIBLE
                viewBinding.sampleItemWithdraw.visibility = View.VISIBLE
                viewBinding.sampleItemDeposit2.visibility = View.VISIBLE
            }
            com.stip.stip.ipasset.model.Filter.DEPOSIT -> {
                // 입금: 입금 관련 트랜잭션만 표시
                viewBinding.sampleItemDeposit.visibility = View.VISIBLE
                viewBinding.sampleItemDeposit2.visibility = View.VISIBLE
                viewBinding.sampleItemWithdraw.visibility = View.GONE
            }
            com.stip.stip.ipasset.model.Filter.WITHDRAW -> {
                // 출금: 출금 관련 트랜잭션만 표시
                viewBinding.sampleItemWithdraw.visibility = View.VISIBLE
                viewBinding.sampleItemDeposit.visibility = View.GONE
                viewBinding.sampleItemDeposit2.visibility = View.GONE
            }
            com.stip.stip.ipasset.model.Filter.REFUND -> {
                // 반환: 반환 관련 트랜잭션만 표시 (이 예제에서는 없음)
                viewBinding.sampleItemDeposit.visibility = View.GONE
                viewBinding.sampleItemWithdraw.visibility = View.GONE
                viewBinding.sampleItemDeposit2.visibility = View.GONE
            }
            else -> {
                // 기타 필터: 모든 트랜잭션 표시
                viewBinding.sampleItemDeposit.visibility = View.VISIBLE
                viewBinding.sampleItemWithdraw.visibility = View.VISIBLE
                viewBinding.sampleItemDeposit2.visibility = View.VISIBLE
            }
        }
        
        // 표시되는 모든 트랜잭션 아이템에 콤마 포맷 적용
        formatTransactionAmount()
        
        // RecyclerView 상태도 필터에 맞게 업데이트
        updateRecyclerView()
    }
    
    /**
     * RecyclerView 설정 - 통화 코드에 따라 적절한 어댑터 사용
     */
    private fun setupRecyclerView() {
        viewBinding.recyclerViewTransactions.layoutManager = LinearLayoutManager(requireContext())
        
        // 통화 코드에 따라 적절한 어댑터 설정
        if (ipAsset.currencyCode == "USD") {
            viewBinding.recyclerViewTransactions.adapter = usdAdapter
        } else {
            // USD가 아닌 티커는 새로운 ticker 전용 어댑터 사용
            viewBinding.recyclerViewTransactions.adapter = tickerAdapter
        }
    }
    
    /**
     * 필터가 변경될 때 RecyclerView 업데이트
     */
    private fun updateRecyclerView() {
        // 필터에 따라 거래 내역 데이터 갱신 처리는 ViewModel + Flow로 자동 처리됨
        // 필요시 추가 로직 구현 가능
    }
    
    /**
     * 거래 내역 상세 화면 표시
     */
    private fun showTransactionDetail(transaction: TransactionHistory) {
        // 통화 코드에 따라 적절한 상세 화면으로 이동
        if (transaction.currencyCode == "USD") {
            if (transaction.status.toString().contains("DEPOSIT")) {
                val intent = Intent(requireContext(), UsdDepositDetailActivity::class.java)
                // 필요한 데이터 전달
                intent.putExtra("usdAmount", transaction.amount)
                intent.putExtra("timestamp", transaction.timestamp)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), UsdWithdrawalDetailActivity::class.java)
                // 필요한 데이터 전달
                intent.putExtra("amount", transaction.amount) 
                intent.putExtra("timestamp", transaction.timestamp)
                startActivity(intent)
            }
        } else {
            // 티커 거래일 경우 티커 전용 상세 화면으로 이동
            val directions = TransactionFragmentDirections
                .actionIpAssetHoldingsFragmentToTickerTransferDetailFragment(transaction.id)
            findNavController().navigate(directions)
        }
    }
    
    /**
     * 거래내역 목록의 금액에 3자리마다 콤마(,) 포맷 적용
     * 뷰가 안전하게 초기화되었는지 확인하고 작업 수행
     */
    private fun formatTransactionAmount() {
        try {
            // 입금 아이템 1 (5,000 USD)
            viewBinding.sampleItemDeposit?.let { depositItem ->
                depositItem.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 5000.0)
                }
                depositItem.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.2f USD", 6500000.0)
                }
            }

            // 출금 아이템 (10,000 USD)
            viewBinding.sampleItemWithdraw?.let { withdrawItem ->
                withdrawItem.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 10000.0)
                }
                withdrawItem.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.2f USD", 13000000.0)
                }
            }

            // 입금 아이템 2 (2,500 USD)
            viewBinding.sampleItemDeposit2?.let { deposit2Item ->
                deposit2Item.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 2500.0)
                }
                deposit2Item.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.2f USD", 3250000.0)
                }
            }
        } catch (e: Exception) {
            // 오류가 발생해도 앱이 크래쉬되지 않도록 예외 처리
            e.printStackTrace()
        }
    }
    
    private fun collectData() {
        lifecycleScope.launch {
            viewBinding.totalHoldings.text = "${String.format("%,.2f", ipAsset.amount.toString().toDoubleOrNull() ?: 0.0)} ${ipAsset.currencyCode}"

            val usdEquivalentValue: Double = try {
                (ipAsset.usdValue as? Number)?.toDouble() ?: 0.0
            } catch (e: Exception) {
                Log.e("TransactionFragment", "Error accessing ipAsset.usdValue", e)
                0.0
            }

            try {
                viewBinding.equivalentAmount.text = "≈ ${String.format("%,.2f", usdEquivalentValue)} USD"
            } catch (e: IllegalArgumentException) {
                viewBinding.equivalentAmount.text = "≈ 0.00 USD"
            }

            // Setup currency icon based on asset type
            setupCurrencyIcon()
        }
    }
    
    /**
     * 통화 아이콘을 설정하는 메서드 - IpAssetListAdapter와 동일한 로직 사용
     * USD와 티커에 따라 다른 아이콘 표시
     */
    private fun setupCurrencyIcon() {
        // 기본적으로 모든 아이콘 숨김
        viewBinding.currencyIconText.visibility = View.GONE
        viewBinding.currencyIconImage.visibility = View.GONE
        
        // 로고 배경 색상 설정
        val backgroundColorRes = when(ipAsset.currencyCode) {
            "USD" -> R.color.token_usd
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
            else -> R.color.token_usd // 기본값
        }
        
        // 배경 색상 적용 (필요한 경우)
        viewBinding.currencyIconBackground?.let { 
            it.backgroundTintList = requireContext().getColorStateList(backgroundColorRes)
        }
        
        if (ipAsset.currencyCode == "USD") {
            // USD인 경우 $ 텍스트 표시
            viewBinding.currencyIconText.apply {
                text = "$"
                visibility = View.VISIBLE
            }
        } else {
            // 티커인 경우 해당 티커의 로고 또는 처음 2자 표시 (동일하게)
            try {
                val resourceId = getTickerLogoResourceId(ipAsset.currencyCode)
                if (resourceId > 0) {
                    // 티커 이미지가 있는 경우 이미지 표시
                    viewBinding.currencyIconImage.apply {
                        setImageResource(resourceId)
                        visibility = View.VISIBLE
                    }
                } else {
                    // 티커 이미지가 없는 경우 처음 2자만 표시 (IpAssetListAdapter와 동일하게)
                    viewBinding.currencyIconText.apply {
                        text = ipAsset.currencyCode.take(2)  // 처음 2자만 표시
                        visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                // 오류 발생시 처음 2자만 표시
                viewBinding.currencyIconText.apply {
                    text = ipAsset.currencyCode.take(2)  // 처음 2자만 표시
                    visibility = View.VISIBLE
                }
            }
        }
    }
    
    /**
     * 티커 코드에 해당하는 로고 리소스 ID 가져오기
     */
    private fun getTickerLogoResourceId(tickerCode: String): Int {
        val context = requireContext()
        val resName = "ic_ticker_${tickerCode.lowercase()}"
        return context.resources.getIdentifier(resName, "drawable", context.packageName)
    }
}
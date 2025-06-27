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
// Transaction history adapters have been removed
// import com.stip.ipasset.ticker.adapter.TickerTransactionHistoryAdapter
import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.ipasset.model.TransactionHistory
import com.stip.stip.iphome.fragment.InfoDialogFragment
import com.stip.stip.ipasset.model.WithdrawalStatus
import com.stip.ipasset.usd.activity.DepositKrwActivity
import com.stip.ipasset.usd.activity.UsdDepositDetailActivity
import com.stip.ipasset.usd.activity.UsdWithdrawalDetailActivity
import com.stip.ipasset.usd.model.USDDepositTransaction
import com.stip.ipasset.usd.model.USDWithdrawalTransaction
import com.stip.ipasset.usd.adapter.USDTransactionAdapter
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
        USDTransactionAdapter { transaction ->
            when (transaction) {
                is USDDepositTransaction -> showUsdDepositDetail(transaction)
                is USDWithdrawalTransaction -> showUsdWithdrawalDetail(transaction)
                else -> Toast.makeText(requireContext(), "Unknown transaction type", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * USD 입금 완료 상세화면으로 이동
     */
    private fun showUsdDepositDetail(transaction: USDDepositTransaction) {
        val intent = Intent(requireContext(), com.stip.ipasset.activity.UsdDepositDetailActivity::class.java)
        intent.putExtra("transaction", transaction)
        startActivity(intent)
    }
    
    /**
     * USD 출금 완료 상세화면으로 이동
     */
    private fun showUsdWithdrawalDetail(transaction: USDWithdrawalTransaction) {
        val intent = Intent(requireContext(), com.stip.ipasset.activity.UsdWithdrawalDetailActivity::class.java)
        intent.putExtra("transaction", transaction)
        startActivity(intent)
    }

    // USD 더미 데이터
    private val usdDummyTransactions = mutableListOf<Any>()

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
        createUSDDummyData() // USD 더미 데이터 생성
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
        
        // UI 업데이트 (USD 트랜잭션 아이템 필터링)
        filterUSDTransactionsBasedOnCurrentFilter()
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
        
        // All sample transaction item references have been removed
        // as part of the transaction history feature removal

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

    // Sample transaction history display methods have been removed
    /**
     * USD 더미 데이터 생성
     */
    private fun createUSDDummyData() {
        // 더미 데이터 목록 초기화
        usdDummyTransactions.clear()
        
        // 입금 완료 더미 데이터
        usdDummyTransactions.add(USDDepositTransaction(
            id = 1001,
            amount = 0.37,
            amountKrw = 500,
            timestamp = 1719014640, // 2025.06.11 10:24
            status = "입금 완료",
            txHash = "0x123abc456def789",
            exchangeRate = 1350.0
        ))
        
        usdDummyTransactions.add(USDDepositTransaction(
            id = 1003,
            amount = 0.22,
            amountKrw = 300,
            timestamp = 1718755920, // 2025.06.07 14:32
            status = "입금 완료",
            txHash = "0x789def456abc123",
            exchangeRate = 1365.0
        ))
        
        // 출금 완료 더미 데이터
        usdDummyTransactions.add(USDWithdrawalTransaction(
            id = 1002,
            amount = 0.15,
            amountKrw = 200,
            timestamp = 1718928180, // 2025.06.10 16:03
            status = "출금 완료",
            txHash = "0xabc123def456789",
            exchangeRate = 1333.0,
            recipientAddress = "0xUserWalletAddress123456789",
            fee = 0.001
        ))
        
        // 초기 필터 적용
        filterUSDTransactionsBasedOnCurrentFilter()
    }
    
    /**
     * USD 트랜잭션 필터링
     */
    private fun filterUSDTransactionsBasedOnCurrentFilter() {
        val filteredItems = when (currentFilter) {
            Filter.ALL -> usdDummyTransactions
            Filter.DEPOSIT -> usdDummyTransactions.filterIsInstance<USDDepositTransaction>()
            Filter.WITHDRAW -> usdDummyTransactions.filterIsInstance<USDWithdrawalTransaction>()
            Filter.REFUND -> emptyList() // 현재 반환 더미데이터 없음
            Filter.PROCESSING -> emptyList() // 현재 진행중 더미데이터 없음
            else -> usdDummyTransactions
        }
        
        // 어댑터에 필터링된 아이템 설정
        usdAdapter.submitList(filteredItems)
        
        // 빈 상태 표시 설정
        viewBinding.emptyStateContainer.visibility = if (filteredItems.isEmpty()) View.VISIBLE else View.GONE
        viewBinding.recyclerViewTransactions.visibility = if (filteredItems.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    // [중복 메서드 제거됨] showUsdDepositDetail 및 showUsdWithdrawalDetail 메서드는 이제 코드 상단에 정의되어 있습니다.
    
    /**
     * 리사이클러뷰 설정
     */
    private fun setupRecyclerView() {
        viewBinding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = usdAdapter
        }
    }
    
    private fun showSampleTransactionHistory() {
        // This method is kept as a stub to maintain API compatibility
        // All sample transaction functionality has been removed
    }
    
    private fun filterTransactionsBasedOnCurrentFilter() {
        // 이 메서드는 더 이상 사용하지 않습니다.
        // 대신 filterUSDTransactionsBasedOnCurrentFilter()를 사용합니다.
        // This method is kept as a stub to maintain API compatibility
        // All sample transaction filtering code has been removed
        
        // Update RecyclerView state based on the filter
        updateRecyclerView()
    }
    
    /**
     * 필터가 변경될 때 RecyclerView 업데이트
     */
    private fun updateRecyclerView() {
        // Check if we have filtered items to show
        val hasItems = usdAdapter.itemCount > 0
        
        // Show/hide empty state based on whether we have items
        viewBinding.emptyStateContainer.visibility = if (!hasItems) View.VISIBLE else View.GONE
        viewBinding.recyclerViewTransactions.visibility = if (hasItems) View.VISIBLE else View.GONE
    }
    
    /**
     * 거래 내역 상세 화면 표시
     */
    // Transaction history detail view has been removed
    private fun showTransactionDetail(transaction: Any) {
        // 기능이 제거되었습니다
        Toast.makeText(requireContext(), "Transaction history feature has been removed", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Transaction history formatting has been removed
     */
    private fun formatTransactionAmount() {
        // All transaction history related code has been removed
        // This method is kept as a stub to maintain API compatibility
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
                // For USD tickers, show KRW conversion
                if (ipAsset.currencyCode == "USD") {
                    // Apply KRW conversion rate (approximate 1 USD = 1,300 KRW)
                    val krwValue = usdEquivalentValue * 1300.0
                    viewBinding.equivalentAmount.text = "≈ ${String.format("%,.2f", krwValue)} KRW"
                } else {
                    viewBinding.equivalentAmount.text = "≈ ${String.format("%,.2f", usdEquivalentValue)} USD"
                }
            } catch (e: IllegalArgumentException) {
                if (ipAsset.currencyCode == "USD") {
                    viewBinding.equivalentAmount.text = "≈ 0.00 KRW"
                } else {
                    viewBinding.equivalentAmount.text = "≈ 0.00 USD"
                }
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
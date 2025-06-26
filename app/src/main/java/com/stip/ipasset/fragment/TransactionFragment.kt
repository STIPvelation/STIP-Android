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

import com.stip.stip.R
import com.stip.stip.databinding.FragmentTransactionBinding
import com.stip.stip.ipasset.TransactionViewModel

import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.iphome.fragment.InfoDialogFragment
import com.stip.stip.ipasset.model.WithdrawalStatus
import com.stip.ipasset.activity.DepositKrwActivity
import com.stip.ipasset.activity.UsdDepositDetailActivity
import com.stip.ipasset.activity.UsdWithdrawalDetailActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log


class TransactionFragment : com.stip.stip.ipasset.fragment.BaseFragment<FragmentTransactionBinding>() {

    private val navArgs: TransactionFragmentArgs by navArgs()
    private val ipAsset: IpAsset get() = navArgs.ipAsset

    private val viewModel by viewModels<TransactionViewModel>()
    // Adapter removed since RecyclerView is no longer used


    private var currentFilter = com.stip.stip.ipasset.model.Filter.ALL

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionBinding {
        return FragmentTransactionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupFilterTabs()
        setupClickListeners()
        showSampleTransactionHistory()
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
            // Start DepositKrwActivity directly when deposit button is clicked
            startActivity(Intent(requireContext(), DepositKrwActivity::class.java))
        }
        
        // Set click listeners for sample transaction items
        viewBinding.sampleItemDeposit.setOnClickListener {
            val intent = Intent(requireContext(), UsdDepositDetailActivity::class.java)
            intent.putExtra("usdAmount", 5000.0)
            intent.putExtra("krwAmount", 6500000.0)
            intent.putExtra("depositorName", "홍길동")
            intent.putExtra("txId", "TX_987654321")
            intent.putExtra("exchangeRate", 1300.0)
            intent.putExtra("fee", 0.0)
            intent.putExtra("status", "입금 완료")
            intent.putExtra("timestamp", System.currentTimeMillis() - 86400000) // Yesterday
            startActivity(intent)
            
            // 리스트 아이템 갱신 - 새로운 콤마 포맷 적용
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
            val intent = Intent(requireContext(), UsdWithdrawalDetailActivity::class.java)
            intent.putExtra("amount", 10000.0)
            intent.putExtra("bankName", "국민은행")
            intent.putExtra("accountNumber", "123-456-789012")
            intent.putExtra("txId", "TX_123456789")
            intent.putExtra("fee", 1.0)
            intent.putExtra("status", "출금 완료")
            intent.putExtra("timestamp", System.currentTimeMillis() - 172800000) // 2 days ago
            startActivity(intent)
            
            // 리스트 아이템 갱신 - 새로운 콤마 포맷 적용
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
            val intent = Intent(requireContext(), UsdDepositDetailActivity::class.java)
            intent.putExtra("usdAmount", 2500.0)
            intent.putExtra("krwAmount", 3250000.0)
            intent.putExtra("depositorName", "김천수")
            intent.putExtra("txId", "TX_567890123")
            intent.putExtra("exchangeRate", 1300.0)
            intent.putExtra("fee", 0.0)
            intent.putExtra("status", "입금 완료")
            intent.putExtra("timestamp", System.currentTimeMillis() - 259200000) // 3 days ago
            startActivity(intent)
            
            // 리스트 아이템 갱신 - 새로운 콤마 포맷 적용
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
        val currentWithdrawalStatus = viewModel.withdrawalStatus.value
        if (currentWithdrawalStatus != null) {
            try {
                val directions = TransactionFragmentDirections
                    .actionIpAssetHoldingsFragmentToWithdrawFragment(currentWithdrawalStatus as WithdrawalStatus)
                findNavController().navigate(directions)
            } catch (e: Exception) {
                Log.e("TransactionFragment", "Withdraw Navigation Error", e)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.withdraw_navigation_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.w("TransactionFragment", "Withdrawal status is null, cannot navigate.")
            Toast.makeText(
                requireContext(),
                getString(R.string.withdraw_status_null_warning),
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
                    it.text = String.format("%,.0f KRW", 6500000.0)
                }
            }

            // 출금 아이템 (10,000 USD)
            viewBinding.sampleItemWithdraw?.let { withdrawItem ->
                withdrawItem.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 10000.0)
                }
                withdrawItem.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.0f KRW", 13000000.0)
                }
            }

            // 입금 아이템 2 (2,500 USD)
            viewBinding.sampleItemDeposit2?.let { deposit2Item ->
                deposit2Item.findViewById<TextView>(R.id.formatted_amount)?.let {
                    it.text = String.format("%,.2f USD", 2500.0)
                }
                deposit2Item.findViewById<TextView>(R.id.usd_amount)?.let {
                    it.text = String.format("%,.0f KRW", 3250000.0)
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
                viewBinding.equivalentAmount.text = "≈ ${String.format("%,.0f", usdEquivalentValue)} KRW"
            } catch (e: IllegalArgumentException) {
                viewBinding.equivalentAmount.text = "≈ 0 KRW"
            }

            // No longer need to update adapter since RecyclerView has been removed
        }
    }
}
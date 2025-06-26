package com.stip.stip.ipasset.fragment

import androidx.core.content.ContextCompat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentTransactionBinding
import com.stip.stip.ipasset.TransactionViewModel
import com.stip.stip.ipasset.adapter.TransactionHistoryListAdapter
import com.stip.stip.ipasset.model.Filter
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.iphome.fragment.InfoDialogFragment
import com.stip.stip.ipasset.model.WithdrawalStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log


class TransactionFragment : com.stip.stip.ipasset.fragment.BaseFragment<FragmentTransactionBinding>() {

    private val navArgs: TransactionFragmentArgs by navArgs()
    private val ipAsset: IpAsset get() = navArgs.ipAsset

    private val viewModel by viewModels<TransactionViewModel>()
    private val adapter = TransactionHistoryListAdapter { item ->
        val isFiat = item.currencyCode.uppercase() in listOf("USD", "KRW")

        val action = if (isFiat) {
            TransactionFragmentDirections
                .actionIpAssetHoldingsFragmentToWithdrawalDetailFragment(transactionId = item.id)
        } else {
            TransactionFragmentDirections
                .actionIpAssetHoldingsFragmentToTickerTransferDetailFragment(transactionId = item.id)
        }

        findNavController().navigate(action)
    }


    private var currentFilter = com.stip.stip.ipasset.model.Filter.ALL

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionBinding {
        return FragmentTransactionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFilterTabs()
        setupClickListeners()

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

        collectData()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setupToolbar() = with(viewBinding.materialToolbar) {
        setNavigationOnClickListener { activity?.finish() }
    }

    private fun setupRecyclerView() = with(viewBinding.recyclerView) {
        adapter = this@TransactionFragment.adapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupFilterTabs() {
        // 필터 탭 클릭 리스너 설정
        viewBinding.tabAll.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.ALL)
            viewModel.applyFilter(com.stip.stip.ipasset.model.Filter.ALL)
        }
        
        viewBinding.tabDeposit.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.DEPOSIT)
            viewModel.applyFilter(com.stip.stip.ipasset.model.Filter.DEPOSIT)
        }
        
        viewBinding.tabWithdrawal.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.WITHDRAW)
            viewModel.applyFilter(com.stip.stip.ipasset.model.Filter.WITHDRAW)
        }
        
        viewBinding.tabReturn.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.REFUND)
            viewModel.applyFilter(com.stip.stip.ipasset.model.Filter.REFUND)
        }
        
        viewBinding.tabInProgress.setOnClickListener {
            updateActiveTab(com.stip.stip.ipasset.model.Filter.PROCESSING)
            viewModel.applyFilter(com.stip.stip.ipasset.model.Filter.PROCESSING)
        }
        
        // 초기 탭 상태 설정 (전체 선택)
        updateActiveTab(com.stip.stip.ipasset.model.Filter.ALL)
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
            if (ipAsset.currencyCode == "USD") {
                navigateToDeposit()
            } else {
                showDepositConfirmDialog()
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
                navigateToDeposit()
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
            val directions = TransactionFragmentDirections
                .actionIpAssetHoldingsFragmentToDepositFragment(ipAsset)
            findNavController().navigate(directions)
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

            launch {
                viewModel.transactionHistories.collect { historyList ->
                    adapter.submitList(historyList)
                }
            }
        }
    }
}
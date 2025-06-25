package com.stip.stip.ipasset.fragment

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log


class TransactionFragment : BaseFragment<FragmentTransactionBinding>() {

    private val navArgs: TransactionFragmentArgs by navArgs()
    private val ipAsset: IpAsset get() = navArgs.ipAsset

    private val viewModel: TransactionViewModel by viewModels()
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


    private val isDropdownMenuShowing = AtomicBoolean(false)
    private val filteredFilters = Filter.entries.filter { it != Filter.OWNED }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTransactionBinding {
        return FragmentTransactionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupDropdownMenu()
        setupClickListeners()

        // 거래 내역 불러오기
        viewModel.fetchTransactionHistory(ipAsset)

        // 출금 계좌 번호 (USD 기준, 다국어 적용)
        val accountNumber = getString(R.string.account_number2)

        // 출금 상태 정보 로딩
        viewModel.fetchWithdrawalStatus(ipAsset, accountNumber)

        collectData()
    }

    override fun onStart() {
        super.onStart()
        setupDropdownAdapter()
    }

    private fun setupToolbar() = with(viewBinding.materialToolbar) {
        setNavigationOnClickListener { activity?.finish() }
    }

    private fun setupRecyclerView() = with(viewBinding.recyclerView) {
        adapter = this@TransactionFragment.adapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupDropdownMenu() = with(viewBinding.dropdownMenu) {
        setDropDownBackgroundDrawable(null)
        setText(filteredFilters.first().getString(requireContext()), false)

        setOnItemClickListener { _, _, position, _ ->
            viewModel.onFilterChange(filteredFilters[position])
        }

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) post { toggleDropdownMenu() }
        }

        setOnClickListener { toggleDropdownMenu() }

        setOnDismissListener {
            lifecycleScope.launch {
                delay(300)
                isDropdownMenuShowing.set(false)
            }
        }
    }

    private fun setupDropdownAdapter() {
        viewBinding.dropdownMenu.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.layout_filter_option,
                filteredFilters.map { it.getString(requireContext()) }
            )
        )
    }

    private fun setupClickListeners() {
        viewBinding.deposit.setOnClickListener {
            if (ipAsset.currencyCode == "USD") {
                navigateToDeposit()
            } else {
                showDepositConfirmDialog()
            }
        }

        viewBinding.withdraw.setOnClickListener {
            if (ipAsset.currencyCode == "USD") {
                navigateToWithdraw()
            } else {
                showWithdrawConfirmDialog()
            }
        }

        viewBinding.withdrawable.setOnClickListener {
            InfoDialogFragment.newInstance(
                titleResId = R.string.withdrawal_limit,
                message = getString(R.string.withdrawal_limit_message),
                titleColorResId = R.color.dialog_title_info_blue
            ).show(parentFragmentManager, InfoDialogFragment.TAG)
        }

        viewBinding.textWithdrawalLimit.setOnClickListener {
            InfoDialogFragment.newInstance(
                titleResId = R.string.common_withdrawable_amount,
                message = getString(R.string.withdrawable_amount_message),
                titleColorResId = R.color.dialog_title_info_blue
            ).show(parentFragmentManager, InfoDialogFragment.TAG)
        }
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
                    .actionIpAssetHoldingsFragmentToWithdrawFragment(currentWithdrawalStatus)
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

    private fun toggleDropdownMenu() {
        val dropdownMenu = viewBinding.dropdownMenu
        when {
            isDropdownMenuShowing.compareAndSet(false, true) -> dropdownMenu.showDropDown()
            isDropdownMenuShowing.compareAndSet(true, false) -> dropdownMenu.dismissDropDown()
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
                viewBinding.totalHoldingsUsdValue.text = String.format("%,.2f USD", usdEquivalentValue)
            } catch (e: IllegalArgumentException) {
                viewBinding.totalHoldingsUsdValue.text = "$0.00 USD"
            }

            launch {
                viewModel.withdrawalStatus.collect { status ->
                    status?.let {
                        viewBinding.pending.text = "${String.format("%,.2f", it.pendingAmount.toString().toDoubleOrNull() ?: 0.0)} ${ipAsset.currencyCode}"
                        viewBinding.withdrawable.text = "${String.format("%,.2f", it.availableAmount.toString().toDoubleOrNull() ?: 0.0)} ${ipAsset.currencyCode}"
                    }
                }
            }

            launch {
                viewModel.transactionHistories.collect { historyList ->
                    adapter.submitList(historyList)
                }
            }
        }
    }
}
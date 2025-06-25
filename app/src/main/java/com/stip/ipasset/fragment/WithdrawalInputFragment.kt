package com.stip.stip.ipasset.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.FragmentWithdrawalInputBinding
import com.stip.stip.ipasset.WithdrawalInputViewModel
import com.stip.stip.ipasset.model.CurrencyFormatter
import com.stip.stip.ipasset.model.WithdrawalDestination
import com.stip.stip.ipasset.model.WithdrawalStatus
import com.stip.stip.signup.keypad.KeypadAdapter
import com.stip.stip.signup.keypad.KeypadItem
import com.stip.stip.signup.keypad.KeypadType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WithdrawalInputFragment() : BaseFragment<FragmentWithdrawalInputBinding>() {
    private val currencyCode: String get() = withdrawalStatus.currencyCode
    private val navArgs: WithdrawalInputFragmentArgs by navArgs()
    private val viewModel by viewModels<WithdrawalInputViewModel>()
    private val withdrawalStatus: WithdrawalStatus get() = navArgs.withdrawalStatus

    private lateinit var keypadAdapter: KeypadAdapter

    private fun bind() {
        viewBinding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewBinding.materialToolbar.title = "$currencyCode ${getString(R.string.common_withdrawal_action)}"

        with(withdrawalStatus) {
            when (withdrawalDestination) {
                is WithdrawalDestination.BankAccount -> {
                    viewBinding.bankContainer.visibility = View.VISIBLE
                    viewBinding.textBank.text = withdrawalDestination.value
                    viewBinding.labelWithdrawalAmount.text = getString(R.string.withdrawal_amount)
                }

                is WithdrawalDestination.Wallet -> {
                    viewBinding.bankContainer.visibility = View.GONE
                    viewBinding.labelWithdrawalAmount.text = getString(R.string.withdrawal_quantity)
                }
            }

            viewBinding.textWithdrawable.text = CurrencyFormatter.format(availableAmount, currencyCode)
            viewBinding.textWithdrawalLimit.text = CurrencyFormatter.format(withdrawalLimit, currencyCode)
            viewBinding.currencyCode.text = currencyCode

            viewBinding.fee.text = CurrencyFormatter.format(fee, "")
            viewBinding.feeCurrency.text = currencyCode
            viewBinding.totalWithdrawalCurrency.text = currencyCode
        }

        setupKeyPad()

        viewBinding.input.setOnClickListener {
            viewModel.showKeypad()
        }

        viewBinding.withdrawalRequest.setOnClickListener {
            findNavController().navigate(
                directions = WithdrawalInputFragmentDirections.actionWithdrawInputFragmentToWithdrawalConfirmFragment(
                    totalWithdrawal = (viewModel.withdrawalAmount.value.toDouble() + withdrawalStatus.fee).toString(),
                    withdrawalStatus = withdrawalStatus
                )
            )
        }
    }

    private fun collect() {
        with(viewModel) {
            lifecycleScope.launch {
                launch {
                    withdrawalStatus.filterNotNull().collect {
                        viewBinding.textBank.text = it.withdrawalDestination.value
                        viewBinding.textWithdrawable.text = CurrencyFormatter.format(it.availableAmount, it.currencyCode)
                        viewBinding.textWithdrawalLimit.text = CurrencyFormatter.format(it.withdrawalLimit, it.currencyCode)
                    }
                }

                launch {
                    isKeypadVisible.collect {
                        viewBinding.rvSignUpPinNumberKeypad.visibility = if (it) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                        val spacerVisibility = if (it) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                        viewBinding.materialDivider.visibility = spacerVisibility
                        viewBinding.spacer1.visibility = spacerVisibility
                        viewBinding.spacer2.visibility = spacerVisibility
                    }
                }

                launch {
                    withdrawalAmount.collect {
                        if (it.isEmpty()) {
                            viewBinding.withdrawalAmount.visibility = View.GONE
                            viewBinding.amountHint.visibility = View.VISIBLE
                        } else {
                            viewBinding.withdrawalAmount.visibility = View.VISIBLE
                            viewBinding.amountHint.visibility = View.GONE
                        }

                        viewBinding.withdrawalAmount.text = it

                        val fee = this@WithdrawalInputFragment.withdrawalStatus.fee
                        val totalWithdrawal = it.toDoubleOrNull()?.plus(fee) ?: fee

                        viewBinding.totalWithdrawal.text = CurrencyFormatter.format(totalWithdrawal, "")
                    }
                }

                launch {
                    withdrawalState.collect {
                        viewBinding.withdrawalRequest.isEnabled = it.isEnabled
                        viewBinding.error.text = it.errorMessage
                    }
                }
            }
        }
    }

    private fun setupKeyPad() {
        val numberItems = (0..9).map { KeypadItem(it.toString(), KeypadType.NUMBER) }.shuffled()
        val fixedItems = listOf(
            KeypadItem(getString(R.string.common_re_order), KeypadType.SHUFFLE),
            numberItems.first(),
            KeypadItem("", KeypadType.DELETE, R.drawable.ic_del_white_31dp)
        )

        val keypadItemList = (numberItems.drop(1) + fixedItems).toMutableList()

        keypadAdapter = KeypadAdapter(keypadItemList) { item ->
            when (item.type) {
                KeypadType.NUMBER -> {
                    viewModel.onNumber(item.value)
                }

                KeypadType.SHUFFLE -> keypadAdapter.shuffleNumbers()
                KeypadType.DELETE -> viewModel.onDelete()
            }
        }

        viewBinding.rvSignUpPinNumberKeypad.adapter = keypadAdapter
        viewModel.showKeypad()
    }

    override fun inflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWithdrawalInputBinding {
        return FragmentWithdrawalInputBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                if (viewModel.isKeypadVisible.value) {
                    viewModel.hideKeypad()
                } else {
                    isEnabled = false

                    findNavController().popBackStack()
                }
            }
        })

        bind()
        collect()
    }
}
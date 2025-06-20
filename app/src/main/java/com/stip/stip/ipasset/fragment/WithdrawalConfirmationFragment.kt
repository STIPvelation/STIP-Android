package com.stip.stip.ipasset.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.FragmentWithdrawalConfirmBinding
import com.stip.stip.ipasset.model.WithdrawalDestination
import com.stip.stip.ipasset.model.WithdrawalStatus

class WithdrawalConfirmationFragment : BaseFragment<FragmentWithdrawalConfirmBinding>(), TransactionDialogFragment.OnClickListener {
    private val navArgs: WithdrawalConfirmationFragmentArgs by navArgs()
    private val totalWithdrawal: String get() = navArgs.totalWithdrawal
    private val withdrawalStatus: WithdrawalStatus get() = navArgs.withdrawalStatus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val withdrawalDestination = withdrawalStatus.withdrawalDestination

        viewBinding.materialToolbar.title = "${withdrawalStatus.currencyCode} ${getString(R.string.common_withdrawal_action)}"
        viewBinding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewBinding.currencyCode.text = withdrawalStatus.currencyCode

        viewBinding.withdrawalDestinationLabel.text = when (withdrawalDestination) {
            is WithdrawalDestination.BankAccount -> getString(R.string.common_withdrawal_account)
            is WithdrawalDestination.Wallet -> getString(R.string.common_withdrawal_wallet)
        }

        viewBinding.withdrawalWarningTitle.text = when (withdrawalDestination) {
            is WithdrawalDestination.BankAccount -> getString(R.string.withdrawal_warning_title1)
            is WithdrawalDestination.Wallet -> getString(R.string.withdrawal_warning_title2)
        }

        viewBinding.labelWithdrawable.text = when (withdrawalDestination) {
            is WithdrawalDestination.BankAccount -> getString(R.string.withdrawal_amount)
            is WithdrawalDestination.Wallet -> getString(R.string.withdrawal_quantity)
        }

        viewBinding.withdrawalConfirmWarningMessage.text = when (withdrawalDestination) {
            is WithdrawalDestination.BankAccount -> getString(R.string.withdrawal_confirm_warning_message1)
            is WithdrawalDestination.Wallet -> getString(R.string.withdrawal_confirm_warning_message2)
        }

        viewBinding.textBank.text = withdrawalDestination.value

        val text = "$totalWithdrawal ${withdrawalStatus.currencyCode}"

        viewBinding.textWithdrawable.text = text

        viewBinding.confirmButton.setOnClickListener {
            TransactionDialogFragment
                .newInstance(
                    getString(R.string.common_notice),
                    getString(R.string.withdrawal_notice_message_withdrawal_completed)
                )
                .show(childFragmentManager, null)
        }
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentWithdrawalConfirmBinding {
        return FragmentWithdrawalConfirmBinding.inflate(inflater, container, false)
    }

    override fun onConfirmButtonClick(dialogFragment: DialogFragment) {
        dialogFragment.dismiss()

        with(findNavController()) {
            popBackStack(
                destinationId = graph.startDestinationId,
                inclusive = false
            )
        }
    }
}

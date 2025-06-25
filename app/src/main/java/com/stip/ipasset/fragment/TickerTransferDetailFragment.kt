package com.stip.stip.ipasset.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.DepositWithdrawCheckTickerBinding
import com.stip.stip.ipasset.TransactionDetailViewModel
import com.stip.stip.ipasset.model.TransactionHistory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TickerTransferDetailFragment : Fragment(R.layout.deposit_withdraw_check_ticker) {

    private var _binding: DepositWithdrawCheckTickerBinding? = null
    private val binding get() = _binding!!

    private val args: TickerTransferDetailFragmentArgs by navArgs()
    private val viewModel: TransactionDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DepositWithdrawCheckTickerBinding.bind(view)

        setupToolbar()
        setupConfirmButton()
        setupTxidCopy()
        observeTransactionDetails()

        Log.d("TickerTransferDetail", "Args => ID: ${args.transactionId}")
        viewModel.fetchTransactionDetailsById(args.transactionId)
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupConfirmButton() {
        binding.confirmButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupTxidCopy() {
        binding.btnCopyTxid.setOnClickListener {
            val txId = binding.valueTxid.text.toString()
            if (txId.isNotBlank()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("TXID", txId)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), getString(R.string.toast_txid_copied), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun observeTransactionDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionDetails.collect { transaction ->
                    transaction?.let {
                        Log.d("TickerTransferDetail", "Received transaction: ${it.status}")
                        updateUi(it)
                    }
                }
            }
        }
    }

    private fun updateUi(transaction: TransactionHistory) {
        val ticker = when {
            !transaction.ticker.isNullOrBlank() && !transaction.ticker.equals("IP", true) && !transaction.ticker.equals("USD", true) -> transaction.ticker
            !transaction.currencyCode.isNullOrBlank() && !transaction.currencyCode.equals("USD", true) -> transaction.currencyCode
            else -> ""
        }
        val amount = transaction.amount / 1_000_000.0
        val executionPrice = transaction.executionPrice
        val usdAmount = amount * executionPrice

        // 헤더와 금액 단위는 반드시 IpHomeFragment에서 내려온 ticker와 100% 연동
        val headerTickerName = transaction.ticker ?: ""
        binding.tvTitle.text = headerTickerName // 헤더에 티커명만 표시

        val (statusText, colorRes) = when (transaction.status) {
            TransactionHistory.Status.DEPOSIT_COMPLETED -> getString(R.string.status_deposit_completed) to R.color.deposit_red
            TransactionHistory.Status.WITHDRAWAL_COMPLETED -> getString(R.string.status_withdrawal_complete) to R.color.withdrawal_blue
        }

        binding.tvStatus.text = statusText
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes))

        binding.tvAmount.text = if (headerTickerName.isNotBlank()) "${formatAmount(transaction.amount)} $headerTickerName" else "${formatAmount(transaction.amount)}"
        binding.tvAmountUsd.text = "${formatUsd(usdAmount)} USD"

        binding.valueCompletionTime.text = formatTimestamp(transaction.timestamp)
        binding.valueType.text = if (transaction.status == TransactionHistory.Status.DEPOSIT_COMPLETED) {
            getString(R.string.value_type_deposit)
        } else {
            getString(R.string.value_type_withdrawal)
        }

        binding.valueNetwork.text = transaction.networkName ?: "Polygon"
        binding.valueTxid.text = transaction.txId ?: "-"

        val isWithdrawal = transaction.status == TransactionHistory.Status.WITHDRAWAL_COMPLETED
        binding.labelTo.visibility = if (isWithdrawal) View.VISIBLE else View.GONE
        binding.valueTo.visibility = if (isWithdrawal) View.VISIBLE else View.GONE
        binding.valueTo.text = if (isWithdrawal) transaction.toAddress ?: "-" else ""
    }

    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            val date = Date(timestamp * 1000)
            sdf.format(date)
        } catch (e: Exception) {
            Log.e("TickerTransferDetail", "Timestamp format error", e)
            "N/A"
        }
    }

    private fun formatAmount(amount: Long): String {
        return try {
            String.format("%,.2f", amount / 1_000_000.0)
        } catch (e: Exception) {
            amount.toString()
        }
    }

    private fun formatUsd(value: Double): String {
        return try {
            String.format("%,.2f", value)
        } catch (e: Exception) {
            value.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

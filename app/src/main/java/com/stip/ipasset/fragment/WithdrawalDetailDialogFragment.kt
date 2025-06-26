package com.stip.stip.ipasset.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.stip.stip.databinding.DialogWithdrawalDetailBinding
import com.stip.stip.ipasset.model.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WithdrawalDetailDialogFragment : DialogFragment() {
    private var _binding: DialogWithdrawalDetailBinding? = null
    private val binding get() = _binding!!
    
    private var onConfirmClickListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWithdrawalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get data from arguments
        val amount = arguments?.getString(KEY_AMOUNT) ?: "0.00"
        val currency = arguments?.getString(KEY_CURRENCY) ?: "USD"
        val fee = arguments?.getString(KEY_FEE) ?: "0.00"
        val bankName = arguments?.getString(KEY_BANK_NAME) ?: ""
        val accountNumber = arguments?.getString(KEY_ACCOUNT_NUMBER) ?: ""
        val currentTime = SimpleDateFormat("yyyy. MM. dd HH:mm", Locale.getDefault()).format(Date())
        
        // 금액 형식화 - 소수점 2자리 및 3자리마다 쉼표 포맷 적용
        val amountDouble = try {
            amount.replace(",", "").replace("$", "").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
        
        val feeDouble = try {
            fee.replace(",", "").replace("$", "").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
        
        // CurrencyFormatter로 숫자 포맷팅 (소수점 2자리, 3자리마다 쉼표) - USD면 $ 기호 추가
        val formattedAmount = if (currency == "USD") {
            "$${CurrencyFormatter.format(amountDouble, "")}"
        } else {
            CurrencyFormatter.format(amountDouble, "")
        }
        
        // 출금 수수료는 무조건 $1.00으로 고정
        val formattedFee = "$1.00"
        
        binding.amount.text = formattedAmount
        binding.currencyValue.text = currency
        binding.withdrawalAmountValue.text = formattedAmount
        binding.feeValue.text = formattedFee
        binding.bankValue.text = bankName
        binding.accountNumberValue.text = accountNumber
        binding.expectedTimeValue.text = "즉시 반영"
        
        // Set click listener
        binding.confirmButton.setOnClickListener {
            onConfirmClickListener?.invoke()
            dismiss()
            
            // "출금 신청이 완료되었습니다." 팝업 표시
            val completionDialog = TransactionDialogFragment.newInstance(
                title = "출금 신청 완료",
                message = "출금 신청이 완료되었습니다."
            )
            
            completionDialog.show(parentFragmentManager, "completion_dialog")
            completionDialog.setOnDismissListener {
                // 다이얼로그가 닫힌 후 메인 화면으로 이동
                val parentFragment = parentFragment
                if (parentFragment != null) {
                    val navController = parentFragment.findNavController()
                    navController.popBackStack(navController.graph.startDestinationId, false)
                }
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun setOnConfirmClickListener(listener: () -> Unit) {
        onConfirmClickListener = listener
    }
    
    companion object {
        private const val KEY_AMOUNT = "amount"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_FEE = "fee"
        private const val KEY_BANK_NAME = "bank_name"
        private const val KEY_ACCOUNT_NUMBER = "account_number"
        
        fun newInstance(
            amount: String,
            currency: String,
            fee: String,
            bankName: String,
            accountNumber: String
        ): WithdrawalDetailDialogFragment {
            return WithdrawalDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_AMOUNT, amount)
                    putString(KEY_CURRENCY, currency)
                    putString(KEY_FEE, fee)
                    putString(KEY_BANK_NAME, bankName)
                    putString(KEY_ACCOUNT_NUMBER, accountNumber)
                }
            }
        }
        
        fun show(
            fragmentManager: FragmentManager,
            amount: String,
            currency: String,
            fee: String,
            bankName: String,
            accountNumber: String
        ): WithdrawalDetailDialogFragment {
            val dialog = newInstance(amount, currency, fee, bankName, accountNumber)
            dialog.show(fragmentManager, "withdrawal_detail_dialog")
            return dialog
        }
    }
}

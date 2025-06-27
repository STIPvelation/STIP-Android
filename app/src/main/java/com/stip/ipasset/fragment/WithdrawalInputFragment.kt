package com.stip.stip.ipasset.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import com.stip.stip.iphome.fragment.InfoDialogFragment
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

        // 출금가능 정보 아이콘 클릭 리스너 설정
        viewBinding.btnWithdrawableInfo.setOnClickListener {
            // iOS SwiftUI 구현과 동일한 커스텀 다이얼로그 표시
            showWithdrawalAvailableInfoDialog()
        }

        // 출금한도 정보 아이콘 클릭 리스너 설정
        viewBinding.btnWithdrawalLimitInfo.setOnClickListener {
            // iOS SwiftUI 구현과 동일한 커스텀 다이얼로그 표시
            showWithdrawalLimitInfoDialog()
        }

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
            viewBinding.textLimit.text = CurrencyFormatter.format(withdrawalLimit, currencyCode)
            viewBinding.tvWithdrawalUnit.text = currencyCode

            viewBinding.feeAmount.text = CurrencyFormatter.format(fee, "") + " " + currencyCode
            // 총출금 초기화 - 금액과 통화 코드 텍스트뷰 각각 설정
            viewBinding.totalWithdrawalAmount.text = "0.00"
            viewBinding.totalWithdrawalCurrency.text = currencyCode
        }

        setupKeyPad()

        // 출금금액 입력란과 컨테이너 모두에 클릭 리스너 추가
        viewBinding.withdrawalInputContainer.setOnClickListener {
            viewModel.showKeypad()
        }
        
        viewBinding.withdrawalInput.setOnClickListener {
            viewModel.showKeypad()
        }

        viewBinding.btnWithdrawalApply.setOnClickListener {
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
                        viewBinding.textLimit.text = CurrencyFormatter.format(it.withdrawalLimit, it.currencyCode)
                    }
                }

                launch {
                    isKeypadVisible.collect {
                        viewBinding.rvKeypad.visibility = if (it) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }

                launch {
                    withdrawalAmount.collect {
                        if (it.isEmpty()) {
                            viewBinding.withdrawalInput.visibility = View.GONE
                            viewBinding.textMinAmount.visibility = View.VISIBLE
                        } else {
                            viewBinding.withdrawalInput.visibility = View.VISIBLE
                            viewBinding.textMinAmount.visibility = View.GONE
                        }

                        // 3자리마다 콤마(,) 추가하여 표시
                        val formattedAmount = if (it.isNotEmpty()) {
                            try {
                                val amount = it.toDoubleOrNull() ?: 0.0
                                CurrencyFormatter.format(amount, "")
                            } catch (e: Exception) {
                                it
                            }
                        } else it
                        
                        viewBinding.withdrawalInput.text = formattedAmount

                        val fee = this@WithdrawalInputFragment.withdrawalStatus.fee
                        val totalWithdrawal = it.toDoubleOrNull()?.plus(fee) ?: fee

                        // 금액과 통화 코드를 따로 표시하여 큰 숫자에도 잘리지 않도록 처리
                        viewBinding.totalWithdrawalAmount.text = CurrencyFormatter.format(totalWithdrawal, "")
                        viewBinding.totalWithdrawalCurrency.text = currencyCode
                    }
                }

                launch {
                    withdrawalState.collect {
                        viewBinding.btnWithdrawalApply.isEnabled = it.isEnabled
                        viewBinding.errorMessage.text = it.errorMessage
                        viewBinding.errorMessage.visibility = if (it.errorMessage?.isNotEmpty() == true) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun setupKeyPad() {
        // 1-9, 0 순서로 기본 정렬된 숫자 생성
        val numberItems = (1..9).map { KeypadItem(it.toString(), KeypadType.NUMBER) } +
                         listOf(KeypadItem("0", KeypadType.NUMBER))
        val fixedItems = listOf(
            KeypadItem("완료", KeypadType.DONE),
            KeypadItem("", KeypadType.DELETE, R.drawable.ic_del_white_31dp)
        )

        val keypadItemList = (numberItems + fixedItems).toMutableList()

        keypadAdapter = KeypadAdapter(keypadItemList) { item ->
            when (item.type) {
                KeypadType.NUMBER -> {
                    viewModel.onNumber(item.value)
                }

                KeypadType.SHUFFLE -> keypadAdapter.setupWithDoneButton()
                KeypadType.DELETE -> viewModel.onDelete()
                KeypadType.DONE -> viewModel.hideKeypad() // 완료 버튼 클릭시 키패드 숨기기
            }
        }

        viewBinding.rvKeypad.adapter = keypadAdapter
        // 초기에는 키패드 숨김
        viewModel.hideKeypad()
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
    
    private fun showInfoDialog(title: String, message: String) {
        InfoDialogFragment.newInstance(
            titleResId = R.string.dialog_title_info, // 실제 표시에는 title 파라미터가 사용됨
            message = message,
            titleColorResId = R.color.dialog_title_info_blue
        ).apply {
            // 타이틀을 직접 문자열로 설정하기 위한 스팬 처리는 InfoDialogFragment 내부에서 수행됨
            setConfirmClickListener {
                // 확인 버튼 클릭 시 아무 작업 없이 닫기
            }
        }.show(parentFragmentManager, InfoDialogFragment.TAG)
    }
    
    private fun showWithdrawalLimitInfoDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_withdrawal_limit_info)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        // 닫기 버튼 클릭 리스너 설정
        dialog.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }
        
        // 확인 버튼 클릭 리스너 설정
        dialog.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showWithdrawalAvailableInfoDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_withdrawal_available_info)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        // 닫기 버튼 클릭 리스너 설정
        dialog.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }
        
        // 확인 버튼 클릭 리스너 설정
        dialog.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
}
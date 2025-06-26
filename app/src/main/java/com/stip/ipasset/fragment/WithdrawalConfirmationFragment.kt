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
import com.stip.stip.ipasset.model.CurrencyFormatter

class WithdrawalConfirmationFragment : BaseFragment<FragmentWithdrawalConfirmBinding>(), TransactionDialogFragment.OnClickListener {
    private val navArgs: WithdrawalConfirmationFragmentArgs by navArgs()
    private val totalWithdrawal: String get() = navArgs.totalWithdrawal
    private val withdrawalStatus: WithdrawalStatus get() = navArgs.withdrawalStatus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val withdrawalDestination = withdrawalStatus.withdrawalDestination

        // 표준 제목 설정
        viewBinding.materialToolbar.title = "${withdrawalStatus.currencyCode} ${getString(R.string.common_withdrawal_action)}"
        viewBinding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        
        // 출금신청 확인 타이틀 설정 - iOS 디자인과 동일하게 파란색으로 하나로 통합
        // 별도의 currencyCode 삭제 및 withdrawal_title로 대체

        // 출금계좌 표시 (실명계좌 또는 지갑)
        viewBinding.withdrawalDestinationLabel.text = when (withdrawalDestination) {
            is WithdrawalDestination.BankAccount -> "출금계좌"
            is WithdrawalDestination.Wallet -> "출금 지갑"
        }
        
        // 은행계좌번호 또는 지갑주소 표시
        viewBinding.textBank.text = withdrawalDestination.value

        // 출금금액 레이블
        viewBinding.labelWithdrawable.text = "출금금액"
        
        // 출금금액 텍스트 설정 - 소수점 2자리 및 3자리마다 쉼표 포맷 적용
        viewBinding.textWithdrawable.text = CurrencyFormatter.format(totalWithdrawal.toDouble(), withdrawalStatus.currencyCode)

        // iOS 스타일의 확인 버튼 및 완료 다이얼로그 처리
        viewBinding.confirmButton.setOnClickListener {
            // 출금 신청 완료 처리 및 실제 API를 호출하는 코드가 여기에 삽입될 수 있음
            // 팝업 다이얼로그 표시
            val bankInfo = withdrawalDestination.value
            val bankName = if (bankInfo.contains(" ")) {
                bankInfo.substring(0, bankInfo.indexOf(" "))
            } else {
                ""
            }
            val accountNumber = if (bankInfo.contains(" ")) {
                bankInfo.substring(bankInfo.indexOf(" ") + 1)
            } else {
                bankInfo
            }
            
            WithdrawalDetailDialogFragment.show(
                childFragmentManager,
                totalWithdrawal,
                withdrawalStatus.currencyCode,
                CurrencyFormatter.format(withdrawalStatus.fee, withdrawalStatus.currencyCode),
                bankName,
                accountNumber
            )
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
    
    private val withdrawalDestination: WithdrawalDestination get() = withdrawalStatus.withdrawalDestination
}

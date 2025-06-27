package com.stip.stip.ipasset.Ticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetTickerWithdrawalConfirmBinding
import com.stip.stip.ipasset.fragment.BaseFragment
import com.stip.stip.ipasset.model.IpAsset
import dagger.hilt.android.AndroidEntryPoint

/**
 * 티커 출금 확인을 처리하는 Fragment
 * 출금 정보를 확인하고 최종 승인합니다.
 */
@AndroidEntryPoint
class TickerWithdrawalConfirmFragment : BaseFragment<FragmentIpAssetTickerWithdrawalConfirmBinding>() {
    private val currencyCode: String get() = ipAsset.currencyCode
    private val ipAsset: IpAsset get() = navArgs.ipAsset
    private val navArgs: TickerWithdrawalConfirmFragmentArgs by navArgs()

    private fun bind() = with(viewBinding) {
        // 툴바 설정
        materialToolbar.title = "$currencyCode ${getString(R.string.common_withdrawal_action)}"
        materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 화면에 표시할 티커 심볼 설정
        tvCurrencyCode.text = currencyCode
        
        // 출금 확인 버튼 리스너
        btnConfirmWithdrawal.setOnClickListener {
            // 실제로는 API 호출하여 출금 요청을 처리
            showWithdrawalRequestComplete()
        }
    }

    private fun showWithdrawalRequestComplete() {
        Toast.makeText(context, "출금 요청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        // 거래내역이나 홈 화면으로 이동
        findNavController().popBackStack(R.id.navigation_transaction_history, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalConfirmBinding {
        return FragmentIpAssetTickerWithdrawalConfirmBinding.inflate(inflater, container, false)
    }
}

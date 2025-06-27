package com.stip.stip.ipasset.Ticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetTickerWithdrawalInputBinding
import com.stip.stip.ipasset.WithdrawalInputViewModel
import com.stip.stip.ipasset.fragment.BaseFragment
import com.stip.stip.ipasset.model.IpAsset
import dagger.hilt.android.AndroidEntryPoint

/**
 * 티커 출금 입력을 처리하는 Fragment
 * 출금 금액과 주소를 입력받습니다.
 */
@AndroidEntryPoint
class TickerWithdrawalInputFragment : BaseFragment<FragmentIpAssetTickerWithdrawalInputBinding>() {
    private val currencyCode: String get() = ipAsset.currencyCode
    private val ipAsset: IpAsset get() = navArgs.ipAsset
    private val navArgs: TickerWithdrawalInputFragmentArgs by navArgs()

    private val viewModel by viewModels<WithdrawalInputViewModel>()

    private fun bind() = with(viewBinding) {
        // 툴바 설정
        materialToolbar.title = "$currencyCode ${getString(R.string.common_withdrawal_action)}"
        materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 출금 계속 버튼 설정
        btnContinue.setOnClickListener {
            navigateToConfirmation()
        }

        // 필드 관련 로직 구현
        // [필요한 로직 추가]
    }

    private fun navigateToConfirmation() {
        // 입력 검증 후 출금 확인 화면으로 이동
        val action = TickerWithdrawalInputFragmentDirections
            .actionTickerWithdrawalInputFragmentToTickerWithdrawalConfirmFragment(ipAsset)
        findNavController().navigate(action)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalInputBinding {
        return FragmentIpAssetTickerWithdrawalInputBinding.inflate(inflater, container, false)
    }
}

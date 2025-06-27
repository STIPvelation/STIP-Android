package com.stip.ipasset.ticker.fragment

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
import androidx.navigation.fragment.navArgs

/**
 * 티커 출금 확인을 처리하는 Fragment
 * 출금 정보를 확인하고 최종 승인합니다.
 */
@AndroidEntryPoint
class TickerWithdrawalConfirmFragment : BaseFragment<FragmentIpAssetTickerWithdrawalConfirmBinding>() {
    private val args by navArgs<com.stip.ipasset.ticker.fragment.TickerWithdrawalConfirmFragmentArgs>()
    private val ipAsset: IpAsset get() = args.ipAsset
    private val currencyCode: String get() = ipAsset.currencyCode

    private fun bind() = with(viewBinding) {
        // 툴바 설정 - 티커 이름 포함 및 중앙 정렬
        materialToolbar.title = "$currencyCode 출금신청"
        materialToolbar.setTitleCentered(true)
        materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 출금 정보 화면에 데이터 바인딩 - 소수점 2자리로 표시
        tvTickerValue.text = currencyCode
        tvAmountValue.text = String.format("%.2f", args.withdrawalAmount)
        tvFeeValue.text = String.format("%.2f", args.fee)
        tvAddressValue.text = args.address
        
        // 총 출금 수량 (출금액 + 수수료) - 소수점 2자리로 표시
        val totalAmount = args.withdrawalAmount + args.fee
        tvTotalAmountValue.text = String.format("%.2f", totalAmount)
        tvTotalAmountCurrency.text = " $currencyCode"
        
        // 주소 복사 버튼 리스너
        btnCopyAddress.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Withdrawal Address", args.address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "주소가 복사되었습니다", Toast.LENGTH_SHORT).show()
        }
        
        // 출금 확인 버튼 리스너 - 확인 다이얼로그 표시
        btnConfirmWithdrawal.setOnClickListener {
            // 확인 버튼 클릭 시 다이얼로그 표시
            showCompletionDialog()
        }
    }

    private fun showCompletionDialog() {
        // 사용자 지정 레이아웃 생성
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_completion, null)
        
        // 다이얼로그 생성 및 사용자 지정 레이아웃 설정
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        // 확인 버튼 찾아서 리스너 설정
        val confirmButton = dialogView.findViewById<android.widget.Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            alertDialog.dismiss()
            // 이전 화면으로 돌아가기
            findNavController().popBackStack()
        }
        
        // 다이얼로그 표시
        alertDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalConfirmBinding {
        return FragmentIpAssetTickerWithdrawalConfirmBinding.inflate(inflater, container, false)
    }
}

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
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("출금 신청 완료")
            .setMessage("출금 신청이 완료되었습니다.")
            .setPositiveButton("확인") { dialog, _ -> 
                dialog.dismiss()
                // 이전 화면으로 돌아가기
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .create()
        
        // 다이얼로그가 보여진 후 텍스트뷰를 찾아서 중앙 정렬 적용
        alertDialog.show()
        
        // 제목 텍스트뷰 중앙 정렬 (두 가지 방식으로 적용)
        val titleId = resources.getIdentifier("alertTitle", "id", "android")
        val titleView = alertDialog.findViewById<android.widget.TextView>(titleId)
        titleView?.apply {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            gravity = android.view.Gravity.CENTER
        }
        
        // 다이얼로그 윈도우에 제목 중앙 정렬 설정
        alertDialog.window?.setGravity(android.view.Gravity.CENTER)
        
        // 메시지 텍스트뷰 중앙 정렬
        val messageView = alertDialog.findViewById<android.widget.TextView>(android.R.id.message)
        messageView?.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetTickerWithdrawalConfirmBinding {
        return FragmentIpAssetTickerWithdrawalConfirmBinding.inflate(inflater, container, false)
    }
}

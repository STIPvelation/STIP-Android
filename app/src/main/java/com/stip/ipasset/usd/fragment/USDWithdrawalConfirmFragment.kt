package com.stip.ipasset.usd.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.MaterialToolbar
import com.stip.ipasset.usd.manager.USDAssetManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetUsdWithdrawalConfirmBinding
import java.text.DecimalFormat

class USDWithdrawalConfirmFragment : Fragment() {

    private var _binding: FragmentIpAssetUsdWithdrawalConfirmBinding? = null
    private val binding get() = _binding!!
    
    // USD 자산 매니저
    private val assetManager = USDAssetManager.getInstance()
    
    // 출금 금액 및 수수료
    private var withdrawalAmount: Double = 0.0
    private var fee: Double = 0.0
    
    // 은행 계좌 더미 데이터
    private val bankName = "신한은행"
    private val accountNumber = "110-123-**6789"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpAssetUsdWithdrawalConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 이전 화면에서 전달받은 데이터 가져오기
        arguments?.let { args ->
            withdrawalAmount = args.getDouble("withdrawal_amount", 0.0)
            fee = args.getDouble("fee", 0.0)
        }
        
        // 툴바 제목 설정 및 뒤로가기 버튼 이벤트 처리
        val toolbar = view.findViewById<MaterialToolbar>(R.id.material_toolbar)
        toolbar.title = "출금신청 확인"
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        
        // 금액 정보 표시
        displayWithdrawalInfo()
        
        // 확인 버튼 이벤트 처리
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            // 출금 요청 처리
            processWithdrawal()
        }
    }
    
    /**
     * 출금 정보 화면에 표시
     */
    private fun displayWithdrawalInfo() {
        val formatter = DecimalFormat("#,##0.00")
        
        // 출금 계좌 표시
        val bankText = view?.findViewById<TextView>(R.id.text_bank)
        bankText?.text = "$bankName $accountNumber"
        
        // 출금 금액 표시
        val withdrawalAmountText = view?.findViewById<TextView>(R.id.text_withdrawable)
        withdrawalAmountText?.text = "${formatter.format(withdrawalAmount)} USD"
        
        // 수수료와 총 출금액은 현재 레이아웃에 없으므로 표시하지 않음
        // 필요시 새로운 TextView를 추가하거나 이 부분은 주석 처리
        android.util.Log.d("USDWithdrawal", "Fee: ${formatter.format(fee)} USD")
        
        // 총 출금액
        val totalAmount = withdrawalAmount + fee
        android.util.Log.d("USDWithdrawal", "Total amount: ${formatter.format(totalAmount)}")
    }
    
    /**
     * 출금 요청 처리
     */
    private fun processWithdrawal() {
        // 출금 처리 - 실제로는 서버에 요청을 보내야 함
        // 여기서는 성공 응답이 온다고 가정
        
        try {
            // 애니메이션 트랜지션 설정
            val fragmentManager = requireActivity().supportFragmentManager
            
            // 출금완료 다이얼로그 표시
            showCompletionDialog()
        } catch (e: Exception) {
            // 오류 처리
            Toast.makeText(requireContext(), "출금 처리 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 출금신청 완료 다이얼로그를 표시합니다.
     */
    private fun showCompletionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_ip_asset_transaction_dialog, null)
        
        // 다이얼로그 내용 설정
        val title = dialogView.findViewById<TextView>(R.id.title)
        val message = dialogView.findViewById<TextView>(R.id.message)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)
        
        title.text = "출금신청"
        message.text = "출금신청이 완료 되었습니다"
        
        // 다이얼로그 생성 및 표시
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.YourCustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        // 확인 버튼 클릭 시 출금 상세 확인 다이얼로그 표시
        confirmButton.setOnClickListener {
            alertDialog.dismiss()
            showWithdrawalDetailConfirmDialog()
        }
        
        alertDialog.show()
    }
    
    /**
     * 출금 상세 확인 다이얼로그를 표시합니다.
     */
    private fun showWithdrawalDetailConfirmDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ip_asset_usd_withdrawal_detail_confirm, null)
        
        // 다이얼로그 내용 설정
        val amountView = dialogView.findViewById<TextView>(R.id.amount)
        val withdrawalAmountValue = dialogView.findViewById<TextView>(R.id.withdrawal_amount_value)
        val currencyValue = dialogView.findViewById<TextView>(R.id.currency_value)
        val feeValue = dialogView.findViewById<TextView>(R.id.fee_value)
        val bankValue = dialogView.findViewById<TextView>(R.id.bank_value)
        val accountNumberValue = dialogView.findViewById<TextView>(R.id.account_number_value)
        val expectedTimeValue = dialogView.findViewById<TextView>(R.id.expected_time_value)
        
        val formatter = DecimalFormat("#,##0.00")
        amountView.text = "$${formatter.format(withdrawalAmount)}"
        withdrawalAmountValue.text = "${formatter.format(withdrawalAmount)} USD"
        currencyValue.text = "USD"
        
        // 수수료 설정 - 고정 1.00 USD
        feeValue.text = "1.00 USD"
        
        // 은행명 표시
        bankValue.text = bankName
        
        // 계좌번호 표시
        accountNumberValue.text = accountNumber
        
        // 예상 출금 시간
        expectedTimeValue.text = "즉시"
        
        // 다이얼로그 생성 및 표시
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.YourCustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        // 다이얼로그 버튼을 찾아서 클릭 이벤트 설정 (레이아웃에 따라 ID 조정 필요)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)
        confirmButton?.setOnClickListener {
            alertDialog.dismiss()
            
            // 출금 처리 - 실제로 잔액 차감
            val success = assetManager.processWithdrawal(withdrawalAmount)
            if (success) {
                // 데이터 갱신 - 모든 화면에서 업데이트된 잔액이 보이도록
                assetManager.refreshData()
                
                // 모든 프래그먼트의 UI 갱신을 위해 추가 처리
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    // 데이터 갱신 메서드를 한 번 더 호출하여 모든 옵저버가 새로운 데이터를 받도록 함
                    assetManager.refreshData()
                    android.util.Log.d("USDWithdrawalConfirm", "Forced additional data refresh for all fragments")
                }, 300)
                
                // 모든 프래그먼트를 종료하고 메인 자산 화면으로 돌아감
                val fragmentManager = requireActivity().supportFragmentManager
                
                // 헤더 레이아웃을 다시 표시 (IpAssetFragment에서 표시되는 상태와 일치하도록)
                val headerLayout = requireActivity().findViewById<View>(R.id.headerLayout)
                headerLayout?.visibility = View.VISIBLE
                
                // 백 스택 처리 - IpAssetFragment로 돌아갈 수 있도록 설정
                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                
                // 트랜잭션 목록 화면으로 이동
                val transaction = fragmentManager.beginTransaction()
                val transactionFragment = USDTransactionFragment()
                transaction.replace(R.id.fragment_container, transactionFragment)
                // 백 스택에 추가하여 뒤로가기 시 IpAssetFragment로 돌아가도록 함
                transaction.addToBackStack("USDTransactionFragment")
                transaction.commit()
            } else {
                // 출금 실패 처리
                Toast.makeText(requireContext(), "출금 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        
        alertDialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

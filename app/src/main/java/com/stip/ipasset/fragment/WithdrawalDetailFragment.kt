package com.stip.stip.ipasset.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.R // R 리소스 사용 위해 필요
import com.stip.stip.databinding.DepositWithdrawCheckTickerBinding
import com.stip.stip.ipasset.model.TransactionHistory // 실제 모델 경로 확인
import com.stip.stip.ipasset.TransactionDetailViewModel // !!! 실제 사용하는 ViewModel 클래스로 변경 !!!
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 입출금 완료 내역 상세 화면 Fragment
 * R.layout.deposit_withdraw_check 레이아웃 사용
 */
class WithdrawalDetailFragment : Fragment(R.layout.deposit_withdraw_check_ticker) {

    // ViewBinding
    private var _binding: DepositWithdrawCheckTickerBinding? = null
    private val binding get() = _binding!!

    // Navigation Argument (transactionId 수신)
    private val args: WithdrawalDetailFragmentArgs by navArgs()

    // ViewModel (!!! 실제 사용하는 ViewModel 클래스로 변경 !!!)
    private val viewModel: TransactionDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DepositWithdrawCheckTickerBinding.bind(view)

        setupToolbar()
        setupConfirmButton()
        observeTransactionDetails()

        // !!! 로그 추가 (Argument 수신 시점) !!!
        Log.d("WithdrawalDetail", "RECEIVED Args => ID: ${args.transactionId}")
        viewModel.fetchTransactionDetailsById(args.transactionId)
    }




    // 툴바 설정 (뒤로가기 버튼)
    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        // 필요 시 공유 버튼 리스너 추가
        // binding.ivShare.setOnClickListener { /* 공유 로직 */ }
    }



    // 확인 버튼 설정
    private fun setupConfirmButton() {
        binding.confirmButton.setOnClickListener {
            findNavController().popBackStack() // 확인 버튼 클릭 시 뒤로가기
        }
    }

    // ViewModel 데이터 관찰 및 UI 업데이트 호출
    private fun observeTransactionDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionDetails.collect { transaction ->
                    transaction?.let {
                        // !!! 로그 추가 (ViewModel 데이터 수신 시점) !!!
                        Log.d("WithdrawalDetail", "RECEIVED from VM => Status: ${it.status}")
                        updateUi(it) // UI 업데이트 호출
                    } ?: run {
                        Log.w("WithdrawalDetail", "Received null transaction details for ID: ${args.transactionId}")
                    }
                }
            }
        }
    }




    /**
     * 전달받은 TransactionHistory 데이터로 UI 요소들을 업데이트하는 함수
     */
    private fun updateUi(transaction: TransactionHistory) {
        // --- 공통 정보 설정 ---
        Log.d("WithdrawalDetail", "amount=${transaction.amount}, ticker=${transaction.ticker}, currencyCode=${transaction.currencyCode}")
        val tickerName = if (transaction.ticker == null || transaction.ticker.equals("IP", true)) transaction.currencyCode else transaction.ticker
        binding.tvAmount.text = formatAmount(transaction.amount, transaction.currencyCode, tickerName)

        binding.valueCompletionTime.text = formatTimestamp(transaction.timestamp) // 완료 시간 포맷팅

        // --- 거래 타입 분류 ---
        val isDeposit = transaction.status == TransactionHistory.Status.DEPOSIT_COMPLETED
        val isWithdrawal = transaction.status == TransactionHistory.Status.WITHDRAWAL_COMPLETED

        // --- 헤더 타이틀 설정 ---
        binding.tvTitle.text = 
            if (isDeposit) "KRW 입금 완료" else "KRW 출금 완료"

        // --- 상태 텍스트 및 색상 설정 ---
        if (isDeposit) {
            binding.tvStatus.text = "입금 완료" 
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_rise))
        } else if (isWithdrawal) {
            binding.tvStatus.text = "출금 완료" 
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_fall))
        } else {
            binding.tvStatus.text = transaction.status.name
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        // --- 유형 텍스트 설정 ---
        binding.valueType.text = 
            when (transaction.currencyCode.uppercase(Locale.ROOT)) {
                "KRW" -> if (isDeposit) "KRW 입금" else "KRW 출금"
                "USD" -> if (isDeposit) "USD 입금" else "USD 출금"
                else -> if (isDeposit) "입금" else "출금"
            }

        // --- 계좌정보 노출 여부 (법정화폐 출금만) ---
        val isFiat = transaction.currencyCode.equals("KRW", true) || transaction.currencyCode.equals("USD", true)
        val showAccountInfo = isFiat && isWithdrawal

        binding.labelAccount.visibility = if (showAccountInfo) View.VISIBLE else View.GONE
        binding.valueAccount.visibility = if (showAccountInfo) View.VISIBLE else View.GONE

        if (showAccountInfo) {
            binding.labelAccount.text = "출금계좌"
            // TODO: 실제 API에서 출금 계좌 정보 가져오기
            // binding.valueAccount.text = transaction.bankAccount
            binding.valueAccount.text = "" // 빈 값 표시
        }
    }




    // 타임스탬프 포맷팅 함수
    private fun formatTimestamp(timestamp: Long): String {
        return try {
            // timestamp 단위(초/밀리초) 확인 필수
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            val date = Date(timestamp * 1000) // 초 단위라고 가정
            sdf.format(date)
        } catch (e: Exception) {
            Log.e("WithdrawalDetail", "Timestamp formatting error", e)
            "N/A"
        }
    }

    // 금액 포맷팅 함수
    private fun formatAmount(amount: Long, currencyCode: String, ticker: String?): String {
        return try {
            // ticker가 "IP"면 currencyCode 사용
            val symbol = if (currencyCode.equals("USD", true)) "USD" else if (ticker == null || ticker.equals("IP", true)) currencyCode else ticker
            val formattedAmount = when (symbol.uppercase(Locale.ROOT)) {
                "USD" -> String.format("%,.2f", amount / 100.0)
                else -> String.format("%,.2f", amount / 10.0)
            }
            "$formattedAmount $symbol"
        } catch (e: Exception) {
            "$amount ${ticker ?: currencyCode}"
        }
    }


    // ViewBinding 참조 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
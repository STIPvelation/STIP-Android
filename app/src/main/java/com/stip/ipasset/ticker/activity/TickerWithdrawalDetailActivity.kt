package com.stip.ipasset.ticker.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stip.stip.databinding.ActivityTickerWithdrawDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class TickerWithdrawalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTickerWithdrawDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTickerWithdrawDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        displayTransactionDetails()
    }

    private fun setupClickListeners() {
        // 뒤로 가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 공유하기 버튼 - 레이아웃에 아직 추가되지 않음
        // 임시로 주석 처리
        /* 향후 ivShare가 추가되면 활성화
        binding.ivShare.setOnClickListener {
            Toast.makeText(this, "공유 기능은 준비중입니다.", Toast.LENGTH_SHORT).show()
        }
        */

        // 트랜잭션 ID 복사
        binding.btnCopyTxid.setOnClickListener {
            val txid = binding.valueTxid.text.toString()
            copyToClipboard("Transaction ID", txid)
            Toast.makeText(this, "거래 ID가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 수신자 주소 복사 버튼은 레이아웃에 없음
        // valueTo를 사용하여 주소 표시
        // 임시로 주석 처리
        /*
        binding.btnCopyTo.setOnClickListener {
            val address = binding.valueTo.text.toString()
            copyToClipboard("Recipient Address", address)
            Toast.makeText(this, "수신자 주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }
        */

        // 확인 버튼
        binding.confirmButton.setOnClickListener {
            finish()
        }
    }

    private fun displayTransactionDetails() {
        // Intent에서 데이터 가져오기
        val tickerSymbol = intent.getStringExtra("tickerSymbol") ?: ""
        val tickerAmount = intent.getDoubleExtra("tickerAmount", 0.0)
        val usdAmount = intent.getDoubleExtra("usdAmount", 0.0)
        val timestamp = intent.getLongExtra("timestamp", 0L)
        val status = intent.getStringExtra("status") ?: ""
        val txHash = intent.getStringExtra("txHash") ?: ""
        val recipientAddress = intent.getStringExtra("recipientAddress") ?: ""
        val fee = intent.getDoubleExtra("fee", 0.0)

        // 화면 타이틀 설정
        binding.tvTitle.text = "출금 내역 상세"

        // 상태 메시지
        binding.tvStatus.text = "출금 완료"

        // 금액 정보
        binding.tvAmount.text = "${tickerAmount.toString()} ${tickerSymbol}"
        binding.tvAmountUsd.text = "≈ ${String.format("%.2f", usdAmount)} USD"

        // 수수료 - 해당 필드는 레이아웃에 없음
        // 임시로 주석 처리
        // binding.valueFee.text = "${fee} ${tickerSymbol}"

        // 시간 포맷팅
        val date = Date(timestamp * 1000)
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm ('UTC' +09:00)", Locale.getDefault())
        binding.valueCompletionTime.text = sdf.format(date)

        // 유형
        binding.valueType.text = "출금"

        // 네트워크
        binding.valueNetwork.text = "Polygon"
        
        // 수신자 주소
        binding.valueTo.text = recipientAddress
        
        // 트랜잭션 ID
        binding.valueTxid.text = txHash
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)
    }
}

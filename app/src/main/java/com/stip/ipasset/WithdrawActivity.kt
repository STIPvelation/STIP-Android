package com.stip.ipasset

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 출금 Activity 예제
 * 실제 사용 시에는 UI 레이아웃과 함께 구현하세요
 */
@AndroidEntryPoint
class WithdrawActivity : AppCompatActivity() {

    private val withdrawViewModel: WithdrawViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 실제 사용 시에는 setContentView(R.layout.activity_withdraw) 와 같이 레이아웃을 설정하세요
        
        setupObservers()
        
        // 예제 출금 요청은 제거하고, 실제 버튼 클릭 시에만 실행되도록 변경
        // exampleWithdrawRequest() // 이 부분은 제거
        
        // 실제 사용 시에는 아래와 같이 버튼 클릭 리스너를 설정하세요
        setupWithdrawButton()
    }

    /**
     * ViewModel 옵저버 설정
     */
    private fun setupObservers() {
        // 로딩 상태 관찰
        lifecycleScope.launch {
            withdrawViewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    Log.d("WithdrawActivity", "출금 요청 중...")
                    // 실제 사용 시에는 프로그레스 바를 표시하세요
                    // progressBar.visibility = View.VISIBLE
                    // withdrawButton.isEnabled = false
                } else {
                    Log.d("WithdrawActivity", "출금 요청 완료")
                    // 실제 사용 시에는 프로그레스 바를 숨기세요
                    // progressBar.visibility = View.GONE
                    // withdrawButton.isEnabled = true
                }
            }
        }

        // 성공 메시지 관찰
        lifecycleScope.launch {
            withdrawViewModel.successMessage.collect { message ->
                message?.let {
                    Log.d("WithdrawActivity", "출금 성공: $it")
                    Toast.makeText(this@WithdrawActivity, "출금 성공: $it", Toast.LENGTH_LONG).show()
                    // 성공 후 입력 필드 초기화
                    // clearInputFields()
                }
            }
        }

        // 에러 메시지 관찰
        lifecycleScope.launch {
            withdrawViewModel.errorMessage.collect { message ->
                message?.let {
                    Log.e("WithdrawActivity", "출금 실패: $it")
                    Toast.makeText(this@WithdrawActivity, "출금 실패: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 출금 버튼 설정
     * 실제 사용 시에는 레이아웃의 버튼에 클릭 리스너를 설정하세요
     */
    private fun setupWithdrawButton() {
        // 실제 사용 시에는 아래와 같이 구현하세요:
        /*
        val withdrawButton = findViewById<Button>(R.id.btn_withdraw)
        val etSymbol = findViewById<EditText>(R.id.et_symbol)
        val etAmount = findViewById<EditText>(R.id.et_amount)
        val etAddress = findViewById<EditText>(R.id.et_address)
        
        withdrawButton.setOnClickListener {
            val symbol = etSymbol.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val address = etAddress.text.toString().trim()
            
            if (validateInput(symbol, amountText, address)) {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                performWithdraw(symbol, amount, address)
            }
        }
        */
        
        // 현재는 예제용으로 Toast 메시지만 표시
        Toast.makeText(this, "출금 버튼 클릭 리스너가 설정되었습니다. 실제 레이아웃에서 버튼을 누르세요.", Toast.LENGTH_LONG).show()
    }

    /**
     * 입력값 검증
     */
    private fun validateInput(symbol: String, amountText: String, address: String): Boolean {
        if (symbol.isEmpty()) {
            Toast.makeText(this, "암호화폐 심볼을 입력하세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (amountText.isEmpty()) {
            Toast.makeText(this, "출금 금액을 입력하세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "올바른 금액을 입력하세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (address.isEmpty()) {
            Toast.makeText(this, "출금 주소를 입력하세요", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    /**
     * 출금 요청 실행
     */
    private fun performWithdraw(symbol: String, amount: Double, address: String) {
        // 메시지 초기화
        withdrawViewModel.clearMessages()
        
        // 출금 요청
        withdrawViewModel.withdrawCrypto(symbol, amount, address)
    }

    /**
     * 예제 출금 요청 (테스트용)
     * 실제 사용 시에는 이 함수를 호출하지 마세요
     */
    fun exampleWithdrawRequest() {
        // 예제 데이터
        val symbol = "BNB"
        val amount = 5.0
        val address = "d60c8468-6fec-4298-bb20-fa7b9e80ce5b"
        
        performWithdraw(symbol, amount, address)
    }
} 
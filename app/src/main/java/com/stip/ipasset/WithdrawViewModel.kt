package com.stip.ipasset

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stip.ipasset.model.WithdrawRequest
import com.stip.ipasset.repository.WalletWithdrawRepository
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 출금 ViewModel
 */
@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val walletWithdrawRepository: WalletWithdrawRepository
) : ViewModel() {

    companion object {
        private const val TAG = "WithdrawViewModel"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * 암호화폐 출금 요청
     * @param symbol 암호화폐 심볼 (예: BNB)
     * @param amount 출금 금액
     * @param address 출금 주소
     */
    fun withdrawCrypto(symbol: String, amount: Double, address: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null

                Log.d(TAG, "=== 출금 요청 시작 ===")
                Log.d(TAG, "symbol: $symbol")
                Log.d(TAG, "amount: $amount")
                Log.d(TAG, "address: $address")

                // 로그인한 사용자 ID 가져오기
                val userId = PreferenceUtil.getUserId()
                Log.d(TAG, "PreferenceUtil.getUserId() 결과: $userId")
                
                if (userId.isNullOrBlank()) {
                    Log.e(TAG, "❌ 사용자 ID가 없습니다!")
                    
                    // 추가 정보 확인
                    val token = PreferenceUtil.getToken()
                    val memberInfo = PreferenceUtil.getMemberInfo()
                    Log.d(TAG, "토큰 존재 여부: ${token != null}")
                    Log.d(TAG, "회원정보 존재 여부: ${memberInfo != null}")
                    Log.d(TAG, "회원정보 ID: ${memberInfo?.id}")
                    
                    // 회원정보에서 ID를 찾을 수 있다면 사용
                    val fallbackUserId = memberInfo?.id
                    if (!fallbackUserId.isNullOrBlank()) {
                        Log.d(TAG, "회원정보에서 ID 찾음: $fallbackUserId")
                        // 다시 저장해보기
                        PreferenceUtil.saveUserId(fallbackUserId)
                        performWithdrawalRequest(fallbackUserId, symbol, amount, address)
                    } else {
                        _errorMessage.value = "로그인 정보를 찾을 수 없습니다. 다시 로그인 해주세요."
                        return@launch
                    }
                } else {
                    Log.d(TAG, "✅ 사용자 ID 확인: $userId")
                    performWithdrawalRequest(userId, symbol, amount, address)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 출금 요청 중 예외 발생", e)
                _errorMessage.value = "출금 요청 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== 출금 요청 완료 ===")
            }
        }
    }

    /**
     * 실제 출금 요청 수행
     */
    private suspend fun performWithdrawalRequest(userId: String, symbol: String, amount: Double, address: String) {
        try {
            Log.d(TAG, "=== API 호출 시작 ===")
            
            val withdrawRequest = WithdrawRequest(
                userId = userId,
                symbol = symbol,
                amount = amount,
                address = address
            )
            
            Log.d(TAG, "전송할 데이터: $withdrawRequest")

            val result = walletWithdrawRepository.withdrawCrypto(withdrawRequest)
            
            result.fold(
                onSuccess = { response ->
                    Log.d(TAG, "✅ 출금 API 호출 성공")
                    Log.d(TAG, "응답: success=${response.success}, message=${response.message}")
                    _successMessage.value = response.message
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ 출금 API 호출 실패: ${error.message}", error)
                    _errorMessage.value = "출금 요청 실패: ${error.message}"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ API 호출 중 예외 발생", e)
            _errorMessage.value = "네트워크 오류가 발생했습니다: ${e.message}"
        }
    }

    /**
     * 메시지 초기화
     */
    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
        Log.d(TAG, "메시지 초기화 완료")
    }
} 
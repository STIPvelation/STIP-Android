package com.stip.ipasset.repository

import android.util.Log
import com.stip.ipasset.api.WalletWithdrawService
import com.stip.ipasset.model.WithdrawRequest
import com.stip.ipasset.model.WithdrawResponse
import com.stip.stip.signup.utils.PreferenceUtil
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 출금 Repository 구현
 */
@Singleton
class WalletWithdrawRepositoryImpl @Inject constructor(
    private val walletWithdrawService: WalletWithdrawService
) : WalletWithdrawRepository {

    companion object {
        private const val TAG = "WalletWithdrawRepo"
    }

    override suspend fun withdrawCrypto(withdrawRequest: WithdrawRequest): Result<WithdrawResponse> {
        return try {
            Log.d(TAG, "🚀 출금 API 호출 시작")
            Log.d(TAG, "📤 요청 데이터: $withdrawRequest")
            
            // 토큰 정보 확인
            val token = PreferenceUtil.getToken()
            Log.d(TAG, "🔐 사용할 토큰: ${if (token != null) "존재" else "없음"}")
            
            val response = walletWithdrawService.withdrawCrypto(withdrawRequest)
            
            Log.d(TAG, "📥 응답 수신: $response")
            
            if (response.success) {
                Log.d(TAG, "✅ 출금 요청 성공: ${response.message}")
                Result.success(response)
            } else {
                Log.e(TAG, "❌ 출금 요청 실패 (서버 응답): ${response.message}")
                Result.failure(Exception("서버 응답 실패: ${response.message}"))
            }
        } catch (e: HttpException) {
            val errorCode = e.code()
            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ex: Exception) {
                "에러 본문 읽기 실패"
            }
            
            Log.e(TAG, "❌ HTTP 에러 발생")
            Log.e(TAG, "   📍 상태 코드: $errorCode")
            Log.e(TAG, "   📄 에러 본문: $errorBody")
            
            val errorMessage = when (errorCode) {
                400 -> "잘못된 요청입니다. 입력 정보를 확인해주세요."
                401 -> "인증이 필요합니다. 다시 로그인해주세요."
                403 -> "권한이 없습니다."
                404 -> "출금 서비스를 찾을 수 없습니다."
                500 -> "서버 내부 오류가 발생했습니다."
                503 -> "서비스를 일시적으로 사용할 수 없습니다."
                else -> "알 수 없는 서버 오류가 발생했습니다. (코드: $errorCode)"
            }
            
            Result.failure(Exception(errorMessage))
        } catch (e: ConnectException) {
            Log.e(TAG, "❌ 네트워크 연결 실패", e)
            Result.failure(Exception("서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요."))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "❌ 연결 시간 초과", e)
            Result.failure(Exception("연결 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."))
        } catch (e: UnknownHostException) {
            Log.e(TAG, "❌ 호스트를 찾을 수 없음", e)
            Result.failure(Exception("서버 주소를 찾을 수 없습니다. 네트워크 연결을 확인해주세요."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ 알 수 없는 오류 발생", e)
            Log.e(TAG, "   🔍 오류 유형: ${e.javaClass.simpleName}")
            Log.e(TAG, "   💬 오류 메시지: ${e.message}")
            
            Result.failure(Exception("출금 요청 중 오류가 발생했습니다: ${e.message}"))
        }
    }
} 
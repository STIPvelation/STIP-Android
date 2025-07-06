package com.stip.ipasset.api

import com.stip.ipasset.model.WithdrawRequest
import com.stip.ipasset.model.WithdrawResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 출금 API 서비스 인터페이스
 */
interface WalletWithdrawService {
    /**
     * 암호화폐 출금 요청
     * @param withdrawRequest 출금 요청 데이터
     * @return 출금 응답 데이터
     */
    @POST("api/wallet/withdraw")
    suspend fun withdrawCrypto(
        @Body withdrawRequest: WithdrawRequest
    ): WithdrawResponse
} 
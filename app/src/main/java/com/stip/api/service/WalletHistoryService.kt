package com.stip.api.service

import com.stip.api.model.WalletHistoryRecord
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletHistoryService {
    @GET("api/wallet/history")
    suspend fun getWalletHistory(
        @Query("userId") userId: String,
        @Query("symbol") symbol: String,
        @Query("type") type: String? = null, // WITHDRAW(출금) / DEPOSIT(입금)
        @Query("status") status: String? = null // REQUEST(진행 중) / APPROVED(거래 완료) / REJECTED(반환)
    ): List<WalletHistoryRecord>
} 
package com.stip.api.service

import com.stip.api.model.WalletHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WalletHistoryService {
    @GET("api/wallet/history")
    suspend fun getWalletHistory(
        @Query("memberId") memberId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): WalletHistoryResponse
} 
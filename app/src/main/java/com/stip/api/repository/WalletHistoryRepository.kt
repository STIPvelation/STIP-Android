package com.stip.api.repository

import com.stip.api.model.WalletHistoryRecord
import com.stip.api.service.WalletHistoryService
import com.stip.stip.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletHistoryRepository {
    private val walletHistoryService: WalletHistoryService by lazy {
        RetrofitClient.createEngineService(WalletHistoryService::class.java)
    }

    suspend fun getWalletHistory(memberId: String, page: Int = 1, limit: Int = 30): List<WalletHistoryRecord> = withContext(Dispatchers.IO) {
        try {
            val response = walletHistoryService.getWalletHistory(memberId, page, limit)
            if (response.success) {
                response.data.record
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 
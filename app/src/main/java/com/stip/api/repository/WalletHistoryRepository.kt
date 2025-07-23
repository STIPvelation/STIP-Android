package com.stip.api.repository

import com.stip.api.model.WalletHistoryRecord
import com.stip.api.service.WalletHistoryService
import com.stip.stip.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletHistoryRepository {
    private val walletHistoryService: WalletHistoryService by lazy {
        RetrofitClient.createTapiService(WalletHistoryService::class.java)
    }

    /**
     * 지갑 거래 내역 조회
     * @param userId 사용자 ID (필수)
     * @param symbol 심볼 (필수)
     * @param type 거래 종류 (옵션) - WITHDRAW(출금) / DEPOSIT(입금)
     * @param status 거래 상태 (옵션) - REQUEST(진행 중) REJECTED(반환)
     * @return 지갑 거래 내역 목록
     */
    suspend fun getWalletHistory(
        userId: String, 
        symbol: String, 
        type: String? = null, 
        status: String? = null
    ): List<WalletHistoryRecord> = withContext(Dispatchers.IO) {
        try {
            val response = walletHistoryService.getWalletHistory(userId, symbol, type, status)
            response
        } catch (e: Exception) {
            emptyList()
        }
    }
} 
package com.stip.stip.api.repository

import com.stip.stip.api.RetrofitClient
import com.stip.stip.api.service.IpListingService
import com.stip.stip.api.service.MarketPairsService
import com.stip.stip.iphome.model.IpListingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * IP 리스팅 데이터 처리를 위한 Repository
 * API 통신을 통해 데이터를 가져오고 앱에서 사용할 수 있는 형태로 변환
 */
class IpListingRepository {
    private val ipListingService: IpListingService by lazy {
        RetrofitClient.createService(IpListingService::class.java)
    }
    
    private val marketPairsService: MarketPairsService by lazy {
        RetrofitClient.createEngineService(MarketPairsService::class.java)
    }

    /**
     * 모든 IP 리스팅 데이터 조회
     * @return IP 리스팅 아이템 목록
     */
    suspend fun getIpListing(): List<IpListingItem> = withContext(Dispatchers.IO) {
        try {
            // Market Pairs API에서 실제 데이터 가져오기
            val response = marketPairsService.getMarketPairs(page = 1, limit = 50)
            
            if (response.success && response.data.record.isNotEmpty()) {
                // API 응답을 IpListingItem으로 변환
                val items = response.data.record.map { it.toIpListingItem() }
                Log.d("IpListingRepository", "API 호출 성공: ${items.size}개 아이템 로드됨")
                items
            } else {
                // API 응답이 성공하지 않거나 데이터가 비어있을 경우
                Log.w("IpListingRepository", "API 응답이 비어있음")
                emptyList()
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 리스트 반환
            Log.e("IpListingRepository", "Market Pairs API 호출 실패: ${e.message}")
            emptyList()
        }
    }

    /**
     * 카테고리별 IP 리스팅 데이터 조회
     * @param category 조회할 카테고리
     * @return 카테고리별 IP 리스팅 아이템 목록
     */
    suspend fun getIpListingByCategory(category: String): List<IpListingItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = ipListingService.getIpListingByCategory(category)
                if (response.isSuccess() && response.data != null) {
                    response.data.items.map { it.toIpListingItem() }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * IP 상세 정보 조회
     * @param ticker IP 티커명
     * @return IP 상세 정보 아이템
     */
    suspend fun getIpDetail(ticker: String): IpListingItem? = withContext(Dispatchers.IO) {
        try {
            val response = ipListingService.getIpDetail(ticker)
            if (response.isSuccess() && response.data != null && response.data.items.isNotEmpty()) {
                response.data.items.first().toIpListingItem()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

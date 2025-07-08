package com.stip.stip.api.repository

import com.stip.stip.api.RetrofitClient
import com.stip.stip.api.service.IpListingService
import com.stip.stip.api.service.MarketPairsService
import com.stip.stip.api.service.TapiMarketPairsService
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
    
    private val tapiMarketPairsService: TapiMarketPairsService by lazy {
        RetrofitClient.createTapiService(TapiMarketPairsService::class.java)
    }

    // 티커와 pairId 매핑을 캐시
    private val tickerToPairIdMap = mutableMapOf<String, String>()

    /**
     * 모든 IP 리스팅 데이터 조회
     * @return IP 리스팅 아이템 목록
     */
    suspend fun getIpListing(): List<IpListingItem> = withContext(Dispatchers.IO) {
        try {
            // TAPI Market Pairs API에서 실제 데이터 가져오기
            val response = tapiMarketPairsService.getMarketPairs(page = 1, limit = 50)
            
            if (response.isNotEmpty()) {
                // API 응답을 IpListingItem으로 변환
                val items = response.map { 
                    // 티커-pairId 매핑 저장
                    tickerToPairIdMap[it.baseAsset] = it.id
                    it.toIpListingItem() 
                }
                Log.d("IpListingRepository", "TAPI API 호출 성공: ${items.size}개 아이템 로드됨")
                items
            } else {
                // API 응답이 비어있을 경우
                Log.w("IpListingRepository", "TAPI API 응답이 비어있음")
                emptyList()
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 리스트 반환
            Log.e("IpListingRepository", "TAPI Market Pairs API 호출 실패: ${e.message}")
            emptyList()
        }
    }

    /**
     * 티커에 해당하는 pairId를 반환
     * @param ticker 티커 심볼
     * @return pairId 또는 null
     */
    suspend fun getPairIdForTicker(ticker: String?): String? {
        if (ticker == null) return null

        // 캐시된 매핑이 있으면 반환
        tickerToPairIdMap[ticker]?.let { return it }

        // 캐시된 매핑이 없으면 API 호출하여 매핑 업데이트
        try {
            val response = tapiMarketPairsService.getMarketPairs(page = 1, limit = 50)
            if (response.isNotEmpty()) {
                response.forEach { pair ->
                    tickerToPairIdMap[pair.baseAsset] = pair.id
                }
            }
        } catch (e: Exception) {
            Log.e("IpListingRepository", "Failed to update ticker-pairId mapping: ${e.message}")
        }

        return tickerToPairIdMap[ticker]
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

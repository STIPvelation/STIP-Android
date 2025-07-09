package com.stip.stip.api.service

import com.stip.stip.api.model.TapiHourlyDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TAPI Hourly Data API 서비스 인터페이스
 * TAPI 서버에서 시간별 거래 데이터를 조회하기 위한 API 정의
 */
interface TapiHourlyDataService {
    
    /**
     * 시간별 거래 데이터 조회
     * @param marketPairId 마켓 페어 ID
     * @param from 시작 시간 (ISO 8601 형식: 2025-06-01T00:00:00)
     * @param to 종료 시간 (ISO 8601 형식: 2025-06-01T23:59:59)
     * @return 시간별 거래 데이터 배열
     */
    @GET("api/tickers/hourly")
    suspend fun getHourlyData(
        @Query("marketPairId") marketPairId: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<TapiHourlyDataResponse>
} 
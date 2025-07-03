package com.stip.stip.api.service

import com.stip.stip.api.model.MarketPairsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Market Pairs API 서비스 인터페이스
 * 엔진 서버에서 Market Pairs 데이터를 조회하기 위한 API 정의
 */
interface MarketPairsService {
    
    /**
     * Market Pairs 데이터 조회
     * @param page 페이지 번호 (기본값: 1)
     * @param limit 페이지당 항목 수 (기본값: 11)
     * @return Market Pairs 데이터 응답
     */
    @GET("api/market/pairs")
    suspend fun getMarketPairs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 11
    ): MarketPairsResponse
} 
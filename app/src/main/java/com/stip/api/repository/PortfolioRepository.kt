package com.stip.api.repository

import android.util.Log
import com.stip.stip.api.RetrofitClient
import com.stip.api.model.PortfolioAsset
import com.stip.api.service.PortfolioService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 포트폴리오 데이터 처리를 위한 Repository
 * API 통신을 통해 포트폴리오 데이터를 가져오고 앱에서 사용할 수 있는 형태로 제공
 */
class PortfolioRepository {
    
    private val portfolioService: PortfolioService by lazy {
        RetrofitClient.createEngineService(PortfolioService::class.java)
    }

    /**
     * 사용자의 포트폴리오 데이터 조회
     * @param memberId 회원 ID
     * @return 포트폴리오 자산 목록
     */
    suspend fun getPortfolio(memberId: String): List<PortfolioAsset> = withContext(Dispatchers.IO) {
        try {
            Log.d("PortfolioRepository", "포트폴리오 API 호출 시작: memberId=$memberId")
            
            val response = portfolioService.getPortfolio(memberId)
            
            Log.d("PortfolioRepository", "API 응답 수신:")
            Log.d("PortfolioRepository", "success: ${response.success}")
            Log.d("PortfolioRepository", "message: ${response.message}")
            Log.d("PortfolioRepository", "data 개수: ${response.data.size}")
            
            if (response.success && response.data.isNotEmpty()) {
                response.data.forEachIndexed { index, asset ->
                    Log.d("PortfolioRepository", "자산 ${index + 1}: ${asset.symbol} = ${asset.balance}")
                }
                
                Log.d("PortfolioRepository", "포트폴리오 API 성공 - ${response.data.size}개 자산 반환")
                response.data
            } else {
                Log.w("PortfolioRepository", "포트폴리오 API 실패 또는 빈 데이터: ${response.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PortfolioRepository", "포트폴리오 API 호출 예외 발생: ${e.message}", e)
            emptyList()
        }
    }
} 
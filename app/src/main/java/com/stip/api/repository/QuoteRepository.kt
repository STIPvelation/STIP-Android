package com.stip.stip.api.repository

import com.stip.stip.api.RetrofitClient
import com.stip.stip.api.service.QuoteService
import com.stip.stip.api.service.QuoteTimeItem
import com.stip.stip.api.service.QuoteDailyItem
import com.stip.stip.iphome.model.PriceChangeStatus
import com.stip.stip.iphome.model.QuoteTick
import com.stip.stip.iphome.model.QuoteTickDaily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * IP 호가 데이터 처리를 위한 Repository
 */
class QuoteRepository {
    private val quoteService: QuoteService by lazy {
        RetrofitClient.createService(QuoteService::class.java)
    }
    
    /**
     * 특정 IP의 시간별 호가 데이터를 가져오는 메소드
     * @param ticker IP 티커 심볼
     * @param limit 가져올 데이터 수
     * @return 시간별 호가 데이터 목록
     */
    suspend fun getTimeQuotes(ticker: String, limit: Int = 30): List<QuoteTick> = withContext(Dispatchers.IO) {
        try {
            val response = quoteService.getTimeQuotes(ticker, limit)
            if (response.isSuccess() && response.data != null) {
                // API 응답 데이터를 앱 내부 모델로 변환
                response.data.items.map { it.toQuoteTick() }
            } else {
                // API 호출 실패 시 빈 목록 반환
                emptyList()
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 목록 반환
            emptyList()
        }
    }
    
    /**
     * 특정 IP의 일별 호가 데이터를 가져오는 메소드
     * @param ticker IP 티커 심볼
     * @param limit 가져올 데이터 수
     * @return 일별 호가 데이터 목록
     */
    suspend fun getDailyQuotes(ticker: String, limit: Int = 20): List<QuoteTickDaily> = withContext(Dispatchers.IO) {
        try {
            val response = quoteService.getDailyQuotes(ticker, limit)
            if (response.isSuccess() && response.data != null) {
                // API 응답 데이터를 앱 내부 모델로 변환
                response.data.items.map { it.toQuoteTickDaily() }
            } else {
                // API 호출 실패 시 빈 목록 반환
                emptyList()
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 목록 반환
            emptyList()
        }
    }
    
    /**
     * API 응답 데이터를 앱 내부 모델로 변환하는 확장 함수
     */
    private fun QuoteTimeItem.toQuoteTick(): QuoteTick {
        val priceChangeStatus = when (this.status) {
            "UP" -> PriceChangeStatus.UP
            "DOWN" -> PriceChangeStatus.DOWN
            else -> PriceChangeStatus.SAME
        }
        
        // 문자열을 숫자로 변환
        val priceValue = this.price.replace(",", "").toDoubleOrNull() ?: 0.0
        val volumeValue = this.amount.replace(",", "").replace("$", "").trim().toDoubleOrNull() ?: 0.0
        
        return QuoteTick(
            id = this.id,
            time = android.text.format.DateFormat.format("HH:mm:ss", this.timestamp).toString(),
            price = priceValue,
            volume = volumeValue,
            priceChangeStatus = priceChangeStatus
        )
    }
    
    /**
     * API 응답 데이터를 앱 내부 모델로 변환하는 확장 함수
     */
    private fun QuoteDailyItem.toQuoteTickDaily(): QuoteTickDaily {
        // 문자열을 숫자로 변환
        val openPriceValue = this.openPrice.replace(",", "").toDoubleOrNull() ?: 0.0
        val highPriceValue = this.highPrice.replace(",", "").toDoubleOrNull() ?: 0.0
        val lowPriceValue = this.lowPrice.replace(",", "").toDoubleOrNull() ?: 0.0
        val closePriceValue = this.closePrice.replace(",", "").toDoubleOrNull() ?: 0.0
        val volumeValue = this.volume.replace(",", "").replace("$", "").trim().toDoubleOrNull() ?: 0.0
        val changePercentValue = this.changePercent.replace("%", "").replace(",", "").toDoubleOrNull() ?: 0.0
        
        return QuoteTickDaily(
            date = this.date,
            openPrice = openPriceValue,
            highPrice = highPriceValue,
            lowPrice = lowPriceValue,
            closePrice = closePriceValue,
            volume = volumeValue,
            changeFromPrevious = closePriceValue - openPriceValue,
            changePercent = changePercentValue
        )
    }
}

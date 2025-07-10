package com.stip.stip.iphome.model

import java.text.SimpleDateFormat
import java.util.*

data class DailyQuote(
    val id: String,
    val date: String,           // 2025-01-01 형식
    val open: Double,           // 시가
    val high: Double,           // 고가
    val low: Double,            // 저가
    val close: Double,          // 종가
    val volume: Double,         // 거래량
    val changePercent: Double   // 변동률
) {
    companion object {
        fun fromTickerData(tickerData: com.stip.stip.iptransaction.model.TickerData): DailyQuote {
            // 현재 날짜를 2025-01-01 형식으로 포맷팅
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            
            return DailyQuote(
                id = tickerData.pairId,
                date = currentDate,
                open = tickerData.lastPrice,
                high = tickerData.lastPrice,
                low = tickerData.lastPrice,
                close = tickerData.lastPrice,
                volume = tickerData.volume,
                changePercent = tickerData.changePercent
            )
        }
    }
} 
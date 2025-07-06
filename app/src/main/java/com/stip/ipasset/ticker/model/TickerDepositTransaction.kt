package com.stip.ipasset.ticker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 티커 입금 완료 트랜잭션 모델
 */
@Parcelize
data class TickerDepositTransaction(
    val id: Long,
    val tickerAmount: Double,
    val tickerSymbol: String,
    val usdAmount: Double,
    val timestamp: Long,
    val timestampIso: String,
    val status: String = "입금 완료",
    val txHash: String? = null
) : Parcelable {
    fun getFormattedTickerAmount(): String = String.format("%,.0f %s", tickerAmount, tickerSymbol)
    fun getFormattedUsdAmount(): String = String.format("%.2f USD", usdAmount)
    
    fun getFormattedDate(): String {
        return try {
            val year = timestampIso.substring(0, 4)
            val month = timestampIso.substring(5, 7)
            val day = timestampIso.substring(8, 10)
            "$year.$month.$day"
        } catch (e: Exception) {
            "----.--.--"
        }
    }
    
    fun getFormattedTime(): String {
        return try {
            timestampIso.substring(11, 16)
        } catch (e: Exception) {
            "--:--"
        }
    }
}

package com.stip.stip.ipasset.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * USD 입금 완료 트랜잭션 모델
 */
@Parcelize
data class USDDepositTransaction(
    val id: Long,
    val amount: Double,
    val amountKrw: Long,
    val timestamp: Long,
    val status: String = "입금 완료",
    val txHash: String? = null,
    val exchangeRate: Double? = null
) : Parcelable {
    fun getFormattedUsdAmount(): String = String.format("%.2f USD", amount)
    fun getFormattedKrwAmount(): String = String.format("%,d KRW", amountKrw)
    
    fun getFormattedDate(): String {
        val date = Date(timestamp * 1000)
        val dateFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }
    
    fun getFormattedTime(): String {
        val date = Date(timestamp * 1000)
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return timeFormat.format(date)
    }
}

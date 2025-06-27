package com.stip.stip.ipasset.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * USD 출금 완료 트랜잭션 모델
 */
@Parcelize
data class USDWithdrawalTransaction(
    val id: Long,
    val amount: Double,
    val amountKrw: Long,
    val timestamp: Long,
    val status: String = "출금 완료",
    val txHash: String? = null,
    val exchangeRate: Double? = null,
    val recipientAddress: String? = null,
    val fee: Double = 0.0
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
    
    fun getFormattedFee(): String = String.format("%.2f USD", fee)
}

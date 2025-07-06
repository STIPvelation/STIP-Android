package com.stip.ipasset.model

import com.google.gson.annotations.SerializedName

/**
 * 출금 응답 데이터 모델
 */
data class WithdrawResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: WithdrawData? = null
)

/**
 * 출금 응답 데이터 내용
 */
data class WithdrawData(
    @SerializedName("transactionId")
    val transactionId: String? = null,
    @SerializedName("txHash")
    val txHash: String? = null,
    @SerializedName("status")
    val status: String? = null
) 
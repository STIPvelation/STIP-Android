package com.stip.ipasset.model

import com.google.gson.annotations.SerializedName

/**
 * 출금 요청 데이터 모델
 */
data class WithdrawRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("address")
    val address: String
) 
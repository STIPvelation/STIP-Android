package com.stip.api.model

import com.google.gson.annotations.SerializedName

/**
 * 포트폴리오 API 응답 데이터 모델
 */
data class PortfolioResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<PortfolioAsset>
)

/**
 * 포트폴리오 자산 아이템
 */
data class PortfolioAsset(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("balance")
    val balance: Double
) 
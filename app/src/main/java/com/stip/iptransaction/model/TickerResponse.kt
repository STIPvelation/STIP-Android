package com.stip.stip.iptransaction.model

import com.google.gson.annotations.SerializedName

data class TickerResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<TickerData> = emptyList()
)

data class TickerData(
    @SerializedName("pairId") val pairId: String = "",
    @SerializedName("symbol") val symbol: String? = null,
    @SerializedName("lastPrice") val lastPrice: Double = 0.0,
    @SerializedName("changePercent") val changePercent: Double = 0.0,
    @SerializedName("high") val high: Double = 0.0,
    @SerializedName("low") val low: Double = 0.0,
    @SerializedName("volume") val volume: Double = 0.0,
    @SerializedName("bids") val bids: List<List<Double>> = emptyList(),
    @SerializedName("asks") val asks: List<List<Double>> = emptyList()
) 
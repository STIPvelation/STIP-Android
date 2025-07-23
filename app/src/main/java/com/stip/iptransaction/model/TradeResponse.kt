package com.stip.stip.iptransaction.model

// 체결 주문 API 응답
data class TradeListResponse(
    val success: Boolean,
    val message: String,
    val data: List<TradeResponse>
)

// 체결 주문 데이터
data class TradeResponse(
    val id: String,
    val price: Double,
    val quantity: Int,
    val timestamp: String,
    val marketPairId: String,
    val buyOrderId: String,
    val sellOrderId: String
) 
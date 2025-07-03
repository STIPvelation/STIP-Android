package com.stip.stip.iptransaction.model

data class UnfilledOrder(
    val orderId: String,
    val memberNumber: String,
    val ticker: String,           // 종목명 (e.g. "AXNO/USD")
    val tradeType: String,        // "매수" or "매도"
    val watchPrice: String,       // 감시가
    val orderPrice: String,       // 주문가
    val orderQuantity: String,    // 주문 수량
    val unfilledQuantity: String, // 미체결 수량
    val orderTime: String         // 주문 시간
)

// API 응답에서 받는 데이터
data class ApiOrderResponse(
    val id: String,
    val userId: String,
    val pairId: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val status: String,
    val filledQuantity: Int,
    val createdAt: String,
    val updatedAt: String
)

// API 응답
data class OrderListResponse(
    val success: Boolean,
    val message: String,
    val data: OrderData
)

data class OrderData(
    val record: List<ApiOrderResponse>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val currentPage: Int,
    val limit: Int,
    val totalRecords: Int,
    val totalPages: Int
) 
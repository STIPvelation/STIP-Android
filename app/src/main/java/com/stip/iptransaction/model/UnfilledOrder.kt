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
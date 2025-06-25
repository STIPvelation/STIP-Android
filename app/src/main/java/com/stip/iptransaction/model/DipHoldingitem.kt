package com.stip.stip.iptransaction.model

data class DipHoldingitem(
    val name: String,
    val quantity: Int,
    val buyPrice: Double,
    val totalValuation: Double,
    val totalBuyAmount: Double,
    val profit: Double,
    val profitRate: Double
)
package com.stip.stip.iptransaction.model

data class MyIpHoldingsSummaryItem(
    val holdingUsd: Double,
    val totalBuy: Double,
    val totalValuation: Double,
    val valuationProfit: Double,
    val profitRate: Double,
    val availableOrder: Double
)
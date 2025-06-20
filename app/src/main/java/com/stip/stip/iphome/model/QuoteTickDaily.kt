package com.stip.stip.iphome.model

data class QuoteTickDaily(
    val id: String,
    val date: String,
    val closePrice: Double,
    val changeFromPrevious: Double,
    val volume: Double
)
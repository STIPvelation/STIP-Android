package com.stip.stip.more.fragment.ipentertainment.model

import java.util.Date

data class AuctionModel(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val currentPrice: Long,
    val endTime: Date,
    val isFeatured: Boolean = false,
    val category: String = "전체"
)

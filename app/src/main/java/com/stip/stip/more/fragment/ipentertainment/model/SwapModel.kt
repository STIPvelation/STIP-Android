package com.stip.stip.more.fragment.ipentertainment.model

data class SwapModel(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val wantedItems: List<String>,
    val isPopular: Boolean = false,
    val category: String = "전체"
)

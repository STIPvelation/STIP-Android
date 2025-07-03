package com.stip.stip.order.data

data class OrderRequest(
    val userId: String,
    val pairId: String,
    val quantity: Int,
    val price: Int
) 
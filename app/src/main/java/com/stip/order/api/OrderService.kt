package com.stip.order.api

import com.stip.order.data.OrderRequest
import com.stip.stip.order.data.OrderDeleteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface OrderService {
    @POST("api/orders/buy")
    suspend fun buyOrder(@Body orderRequest: OrderRequest): Response<OrderResponse>

    @POST("api/orders/sell")
    suspend fun sellOrder(@Body orderRequest: OrderRequest): Response<OrderResponse>
    
    @DELETE("api/orders/{orderId}")
    suspend fun deleteOrder(@Path("orderId") orderId: String): Response<OrderDeleteResponse>
}

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val data: OrderData?
)

data class OrderData(
    val id: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val filledQuantity: Int,
    val status: String,
    val member: Any?,
    val marketPair: Any?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?
) 
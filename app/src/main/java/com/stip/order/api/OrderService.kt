package com.stip.order.api

import com.stip.order.data.OrderRequest
import com.stip.stip.order.data.OrderDeleteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface OrderService {
    @POST("/api/orders/buy")
    suspend fun buyOrder(@Body orderRequest: OrderRequest): Response<Unit>

    @POST("/api/orders/sell")
    suspend fun sellOrder(@Body orderRequest: OrderRequest): Response<Unit>
    
    @DELETE("/api/orders/{orderId}")
    suspend fun deleteOrder(@Path("orderId") orderId: String): Response<OrderDeleteResponse>
} 
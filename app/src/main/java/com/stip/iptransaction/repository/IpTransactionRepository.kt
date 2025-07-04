package com.stip.stip.iptransaction.repository

import com.stip.order.api.OrderService
import com.stip.stip.api.RetrofitClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IpTransactionRepository @Inject constructor() {
    private val orderService: OrderService = RetrofitClient.createOrderService()
    
    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            val response = orderService.deleteOrder(orderId)
            if (response.isSuccessful) {
                val deleteResponse = response.body()
                if (deleteResponse?.success == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(deleteResponse?.message ?: "주문 삭제에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("HTTP 오류: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
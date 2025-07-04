package com.stip.api.model

import com.google.gson.annotations.SerializedName

// 입출금 내역(거래 내역) 단일 레코드
data class WalletHistoryRecord(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("type") val type: String, // deposit, withdraw
    @SerializedName("amount") val amount: Double,
    @SerializedName("timestamp") val timestamp: String
)

// 입출금 내역 API 응답
data class WalletHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: WalletHistoryData
)

data class WalletHistoryData(
    @SerializedName("record") val record: List<WalletHistoryRecord>,
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalRecords") val totalRecords: Int,
    @SerializedName("totalPages") val totalPages: Int
) 
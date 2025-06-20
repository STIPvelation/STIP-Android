package com.stip.stip.more.model

data class LoginHistoryItem(
    val timestamp: String,
    val ipAddress: String,
    val deviceInfo: String,
    val location: String
)


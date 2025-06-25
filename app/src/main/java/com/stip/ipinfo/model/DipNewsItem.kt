package com.stip.stip.ipinfo.model

data class DipNewsItem(
    val logoResId: Int?,       // ✅ 로고는 nullable
    val name: String,          // ✅ DIP 이름
    val description: String,   // ✅ DIP 설명
    val date: String           // ✅ 날짜
)
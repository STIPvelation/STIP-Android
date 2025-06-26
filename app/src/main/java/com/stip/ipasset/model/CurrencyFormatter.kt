package com.stip.stip.ipasset.model

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true // 3자리마다 콤마 표시
    }

    fun format(amount: Number, currencyCode: String): String {
        val number = numberFormat.format(amount)
        
        return if (currencyCode.isEmpty()) number else "$number $currencyCode"
    }
}

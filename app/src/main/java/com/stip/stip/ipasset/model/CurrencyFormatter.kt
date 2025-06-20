package com.stip.stip.ipasset.model

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun format(amount: Number, currencyCode: String): String {
        val number = numberFormat.format(amount)

        return "$number $currencyCode"
    }
}

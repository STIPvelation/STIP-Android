package com.stip.stip.ipasset.model

import android.icu.text.NumberFormat
import android.os.Parcelable
import com.stip.stip.ipasset.extension.isUSD
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class IpAsset(
    val id: Long,
    val currencyCode: String,
    val amount: Long,
    val usdValue: Long
) : Parcelable {
    val isUSD: Boolean get() = currencyCode.isUSD
    val formattedAmount: String get() = "${numberFormat.format(amount)} $currencyCode"
    val formattedUsdValue: String get() = "${numberFormat.format(usdValue)} USD"

    companion object {
        private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        const val KEY: String = "ipAsset"
        const val NAME: String = KEY
    }
}

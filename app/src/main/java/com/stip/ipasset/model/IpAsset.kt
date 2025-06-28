package com.stip.ipasset.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IpAsset(
    val id: String,
    val name: String,
    val ticker: String,
    val balance: Double,
    val value: Double,
    val currencyCode: String = ticker,
    val amount: Double = balance
) : Parcelable

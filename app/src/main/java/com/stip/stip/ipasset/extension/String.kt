package com.stip.stip.ipasset.extension

import android.icu.util.Currency
import java.util.Locale

val String.isUSD: Boolean get() = lowercase() == Currency.getInstance(Locale.US).currencyCode.lowercase()

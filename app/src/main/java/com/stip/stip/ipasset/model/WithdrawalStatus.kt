package com.stip.stip.ipasset.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WithdrawalStatus(
    val pendingAmount: Double,
    val availableAmount: Double,
    val withdrawalLimit: Double,
    val currencyCode: String,
    val fee: Double,
    val withdrawalDestination: WithdrawalDestination,
) : Parcelable

package com.stip.stip.ipasset.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class WithdrawalDestination : Parcelable {
    abstract val value: String

    @Parcelize
    data class BankAccount(override val value: String) : WithdrawalDestination()

    @Parcelize
    data class Wallet(override val value: String) : WithdrawalDestination()
}

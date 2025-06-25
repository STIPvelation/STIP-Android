package com.stip.stip.ipasset.model

import android.content.Context
import androidx.annotation.StringRes
import com.stip.stip.R

enum class Filter(@StringRes val id: Int) {
    ALL(R.string.filter_all),
    OWNED(R.string.filter_owned),   // OWNED 필터 추가
    DEPOSIT(R.string.filter_deposit),
    WITHDRAW(R.string.filter_withdraw),
    REFUND(R.string.filter_refund),
    PENDING(R.string.filter_pending);

    fun getString(context: Context) = context.getString(id)
}

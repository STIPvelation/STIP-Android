package com.stip.stip.ipasset.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.TypedValue

fun Context.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    )
}

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)

    clipboard.setPrimaryClip(clip)
}

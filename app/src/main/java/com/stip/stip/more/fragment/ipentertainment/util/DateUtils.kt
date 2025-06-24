package com.stip.stip.more.fragment.ipentertainment.util

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 남은 시간을 "n일 n시간" 형태의 텍스트로 포맷팅
 */
fun formatRemainingTime(endTime: Date): String {
    val currentTime = Date()
    
    // 이미 종료된 경우
    if (endTime.before(currentTime)) {
        return "종료됨"
    }
    
    val diffInMillis = endTime.time - currentTime.time
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
    val remainingHoursMillis = diffInMillis - TimeUnit.DAYS.toMillis(diffInDays)
    val diffInHours = TimeUnit.MILLISECONDS.toHours(remainingHoursMillis)
    
    return when {
        diffInDays > 0 && diffInHours > 0 -> "${diffInDays}일 ${diffInHours}시간"
        diffInDays > 0 -> "${diffInDays}일"
        diffInHours > 0 -> "${diffInHours}시간"
        else -> "${TimeUnit.MILLISECONDS.toMinutes(diffInMillis)}분"
    }
}

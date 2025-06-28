package com.stip.dummy

/**
 * FeeAndLimitsDummyData를 위한 확장 함수
 */

/**
 * 특정 통화의 출금 수수료를 반환합니다.
 */
fun FeeAndLimitsDummyData.getWithdrawalFee(currency: String): Double {
    return when(currency) {
        "USD" -> 3.0
        else -> 1.0
    }
}

/**
 * 특정 통화의 최대 출금 한도를 반환합니다.
 */
fun FeeAndLimitsDummyData.getMaxWithdrawalAmount(currency: String): Double {
    return when(currency) {
        "USD" -> 498999.00
        else -> 100000.0
    }
}

/**
 * 특정 통화의 최소 출금 한도를 반환합니다.
 */
fun FeeAndLimitsDummyData.getMinWithdrawalAmount(currency: String): Double {
    return when(currency) {
        "USD" -> 10.0
        else -> 1.0
    }
}

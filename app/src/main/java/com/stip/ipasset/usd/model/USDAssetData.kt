package com.stip.ipasset.usd.model

/**
 * USD 자산 데이터 모델
 */
data class USDAssetData(
    val balance: Double = 10000.00,        // 총 USD 잔액
    val withdrawable: Double = 8999.00,   // 출금 가능 금액
    val withdrawalLimit: Double = 498999.00, // 출금 한도
    val fee: Double = 3.00               // 출금 수수료
)

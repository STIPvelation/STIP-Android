package com.stip.dummy

import com.stip.ipasset.model.IpAsset

/**
 * 입출금 관련 더미 데이터를 중앙화하여 관리하는 클래스
 * - 모든 화면에서 일관된 더미 데이터를 사용할 수 있도록 함
 * - 테스트용으로만 사용되며, 실제 배포에서는 API 또는 DB에서 데이터 로드
 */
object AssetDummyData {
    
    // 변경 가능한 자산 리스트 (업데이트를 위해 mutable로 변경)
    private val assets = mutableListOf(
        // USD 데이터
        IpAsset(id = "1", name = "US Dollar", ticker = "USD", balance = 10000.0, value = 10000.0),
        
        // 11개 티커 데이터 (iOS TransactionView.swift 기반)
        IpAsset(id = "2", name = "JWV Token", ticker = "JWV", balance = 350.0, value = 420.0),
        IpAsset(id = "3", name = "MDM Token", ticker = "MDM", balance = 200.0, value = 280.0),
        IpAsset(id = "4", name = "CDM Token", ticker = "CDM", balance = 0.0, value = 0.0),   // 0개 소유 예시
        IpAsset(id = "5", name = "IJECT Token", ticker = "IJECT", balance = 500.0, value = 650.0),
        IpAsset(id = "6", name = "WETALK Token", ticker = "WETALK", balance = 120.0, value = 130.0),
        IpAsset(id = "7", name = "SLEEP Token", ticker = "SLEEP", balance = 0.0, value = 0.0),  // 0개 소유 예시
        IpAsset(id = "8", name = "KCOT Token", ticker = "KCOT", balance = 800.0, value = 1100.0),
        IpAsset(id = "9", name = "MSK Token", ticker = "MSK", balance = 50.0, value = 70.0),
        IpAsset(id = "10", name = "SMT Token", ticker = "SMT", balance = 0.0, value = 0.0),   // 0개 소유 예시
        IpAsset(id = "11", name = "AXNO Token", ticker = "AXNO", balance = 150.0, value = 180.0),
        IpAsset(id = "12", name = "KATV Token", ticker = "KATV", balance = 20.0, value = 20.0)
    )
    
    /**
     * 기본 자산 더미 데이터 반환
     */
    fun getDefaultAssets(): List<IpAsset> {
        return assets.toList()
    }
    
    /**
     * 특정 ID의 티커 정보 반환
     */
    fun getAssetById(id: String): IpAsset? {
        return getDefaultAssets().find { it.id == id }
    }
    
    /**
     * 특정 코드의 티커 정보 반환
     */
    fun getAssetByCode(code: String): IpAsset? {
        return getDefaultAssets().find { it.currencyCode == code }
    }
    
    /**
     * 특정 코드의 자산 업데이트 (출금/입금 처리)
     * @param code 티커 코드
     * @param amount 변경할 금액 (출금은 음수, 입금은 양수)
     * @return 업데이트 성공 여부
     */
    fun updateAssetAmount(code: String, amount: Float): Boolean {
        val assetIndex = assets.indexOfFirst { it.ticker == code }
        if (assetIndex != -1) {
            val asset = assets[assetIndex]
            // 현재 자산에서 변경할 금액을 더함 (출금은 음수를 전달하므로 차감됨)
            val newBalance = asset.balance + amount
            
            // 잔액이 부족한 경우 처리
            if (newBalance < 0) return false
            
            // 새 자산 객체를 생성하여 기존 자산을 교체 (value는 1:1 비율로 간단히 계산)
            assets[assetIndex] = asset.copy(
                balance = newBalance,
                value = newBalance // 간단하게 1:1 비율로 처리
            )
            return true
        }
        return false
    }
    
    /**
     * 총 보유 USD 가치 반환
     */
    fun getTotalUsdValue(): Double {
        return getDefaultAssets().sumOf { it.value }
    }
}

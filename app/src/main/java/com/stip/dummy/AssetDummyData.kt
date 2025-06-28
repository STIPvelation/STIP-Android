package com.stip.dummy

import com.stip.ipasset.model.IpAsset

/**
 * 입출금 관련 더미 데이터를 중앙화하여 관리하는 클래스
 * - 모든 화면에서 일관된 더미 데이터를 사용할 수 있도록 함
 * - 테스트용으로만 사용되며, 실제 배포에서는 API 또는 DB에서 데이터 로드
 */
object AssetDummyData {
    
    /**
     * 기본 자산 더미 데이터 반환
     */
    fun getDefaultAssets(): List<IpAsset> {
        return listOf(
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
     * 총 보유 USD 가치 반환
     */
    fun getTotalUsdValue(): Double {
        return getDefaultAssets().sumOf { it.value }
    }
}

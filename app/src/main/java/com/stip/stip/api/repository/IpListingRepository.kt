package com.stip.stip.api.repository

import com.stip.stip.api.RetrofitClient
import com.stip.stip.api.service.IpListingService
import com.stip.stip.iphome.model.IpListingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * IP 리스팅 데이터 처리를 위한 Repository
 * API 통신을 통해 데이터를 가져오고 앱에서 사용할 수 있는 형태로 변환
 */
class IpListingRepository {
    private val ipListingService: IpListingService by lazy {
        RetrofitClient.createService(IpListingService::class.java)
    }

    /**
     * 모든 IP 리스팅 데이터 조회
     * @return IP 리스팅 아이템 목록
     */
    suspend fun getIpListing(): List<IpListingItem> = withContext(Dispatchers.IO) {
        try {
            createDummyIpListingData()
//            val response = ipListingService.getIpListing()
//            if (response.isSuccess() && response.data != null) {
//                // DTO 모델을 앱 내부 모델로 변환
//                response.data.items.map { it.toIpListingItem() }
//            } else {
//                // API 호출 실패 시 더미 데이터 반환
//                Log.d("IpListingRepository", "API call failed: returning dummy data")
//                createDummyIpListingData()
//            }
        } catch (e: Exception) {
            // 예외 발생 시 더미 데이터 반환
            Log.e("IpListingRepository", "Exception during API call: ${e.message}")
            createDummyIpListingData()
        }
    }

    /**
     * 카테고리별 IP 리스팅 데이터 조회
     * @param category 조회할 카테고리
     * @return 카테고리별 IP 리스팅 아이템 목록
     */
    suspend fun getIpListingByCategory(category: String): List<IpListingItem> =
        withContext(Dispatchers.IO) {
            try {
                val response = ipListingService.getIpListingByCategory(category)
                if (response.isSuccess() && response.data != null) {
                    response.data.items.map { it.toIpListingItem() }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * IP 상세 정보 조회
     * @param ticker IP 티커명
     * @return IP 상세 정보 아이템
     */
    suspend fun getIpDetail(ticker: String): IpListingItem? = withContext(Dispatchers.IO) {
        try {
            val response = ipListingService.getIpDetail(ticker)
            if (response.isSuccess() && response.data != null && response.data.items.isNotEmpty()) {
                response.data.items.first().toIpListingItem()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 더미 IP 리스팅 데이터 생성
     * API 호출 실패 시 사용
     */
    private fun createDummyIpListingData(): List<IpListingItem> {
        return listOf(
            IpListingItem(
                logoResId = null,
                ticker = "JWV",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            ),
            IpListingItem(
                logoResId = null,
                ticker = "MDM",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            ),
            IpListingItem(
                logoResId = null,
                ticker = "CDM",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            ),
            IpListingItem(
                logoResId = null,
                ticker = "IJECT",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "WETALK",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "SLEEP",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "KCOT",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "MSK",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "SMT",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "Patent",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "AXNO",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "BM",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
            ,IpListingItem(
                logoResId = null,
                ticker = "KATV",
                currentPrice = "0",
                changePercent = "0",
                changeAbsolute = "0",
                volume = "0",
                category = "BM",
                companyName = "",
                high24h = "0",
                low24h = "0",
                volume24h = "0",
                open = "0",
                high = "0",
                low = "0",
                close = "0",
                isTradeTriggered = false,
                isBuy = false,
                type = "",
                registrationNumber = "0",
                firstIssuanceDate = "0",
                totalIssuanceLimit = "0",
                linkBlock = "0",
                linkRating = "0",
                linkLicense = "0",
                linkVideo = "0",
                currentCirculation = "0",
                linkDigitalIpPlan = "0",
                linkLicenseAgreement = "0",
                digitalIpLink = "0",
                businessPlanLink = "0",
                relatedVideoLink = "0",
                homepageLink = "0"
            )
        )
    }
}

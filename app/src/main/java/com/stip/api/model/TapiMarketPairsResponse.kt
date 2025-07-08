package com.stip.stip.api.model

import com.google.gson.annotations.SerializedName
import com.stip.stip.iphome.model.IpListingItem

/**
 * TAPI Market Pairs API 응답 모델
 * 실제 응답은 배열 형태: [{...}, {...}]
 */
data class TapiMarketPairsResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("symbol")
    val symbol: String,
    
    @SerializedName("baseAsset")
    val baseAsset: String,
    
    @SerializedName("quoteAsset")
    val quoteAsset: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("createdAt")
    val createdAt: String
) {
    /**
     * TapiMarketPairsResponse를 IpListingItem으로 변환
     */
    fun toIpListingItem(): IpListingItem {
        // 공백 제거 및 데이터 정리
        val cleanBaseAsset = baseAsset.trim()
        
        return IpListingItem(
            ticker = cleanBaseAsset,
            currentPrice = "0.00",
            changePercent = "0.00%",
            changeAbsolute = "0.00",
            volume = "0",
            category = "Patent",    // 추가 필요
            companyName = cleanBaseAsset,
            high24h = "0.00",
            low24h = "0.00",
            volume24h = "0",
            open = "0.00",
            high = "0.00",
            low = "0.00",
            close = "0.00",
            isTradeTriggered = false,
            isBuy = false,
            type = "",
            registrationNumber = id,
            firstIssuanceDate = createdAt.split("T")[0], // 날짜 부분만 추출
            totalIssuanceLimit = "1,000,000",
            linkBlock = null,
            linkRating = null,
            linkLicense = null,
            linkVideo = null,
            currentCirculation = "0",
            linkDigitalIpPlan = null,
            linkLicenseAgreement = null,
            digitalIpLink = null,
            businessPlanLink = null,
            relatedVideoLink = null,
            homepageLink = null,
            patentGrade = null,
            institutionalValues = null,
            stipValues = null,
            usagePlanData = null,
            representative = null,
            businessType = null,
            contactEmail = null,
            address = null,
            snsTwitter = null,
            snsInstagram = null,
            snsKakaoTalk = null,
            snsTelegram = null,
            snsLinkedIn = null,
            snsWeChat = null
        )
    }
} 
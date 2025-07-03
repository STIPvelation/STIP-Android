package com.stip.stip.api.model

import com.google.gson.annotations.SerializedName
import com.stip.stip.iphome.model.IpListingItem

/**
 * Market Pairs API 응답 모델
 */
data class MarketPairsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: MarketPairsData
)

data class MarketPairsData(
    @SerializedName("record")
    val record: List<MarketPairItem>,
    
    @SerializedName("pagination")
    val pagination: Pagination
)

data class MarketPairItem(
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
    val createdAt: String,

    @SerializedName("lastPrice")
    val lastPrice: Double = 0.0,

    @SerializedName("changePercent")
    val changePercent: Double = 0.0,

    @SerializedName("volume")
    val volume: Double = 0.0,

    @SerializedName("high")
    val high: Double = 0.0,

    @SerializedName("low")
    val low: Double = 0.0,

    @SerializedName("open")
    val open: Double = 0.0,

    @SerializedName("close")
    val close: Double = 0.0
) {
    /**
     * MarketPairItem을 IpListingItem으로 변환
     */
    fun toIpListingItem(): IpListingItem {
        // 공백 제거 및 데이터 정리
        val cleanBaseAsset = baseAsset.trim()
        
        return IpListingItem(
            ticker = cleanBaseAsset,
            currentPrice = String.format("%.2f", lastPrice),
            changePercent = String.format("%+.2f%%", changePercent),
            changeAbsolute = String.format("%+.2f", lastPrice * changePercent / 100),
            volume = String.format("%.0f", volume),
            category = "Patent", // 기본값
            companyName = cleanBaseAsset,
            high24h = String.format("%.2f", high),
            low24h = String.format("%.2f", low),
            volume24h = String.format("%.0f", volume),
            open = String.format("%.2f", open),
            high = String.format("%.2f", high),
            low = String.format("%.2f", low),
            close = String.format("%.2f", close),
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

data class Pagination(
    @SerializedName("currentPage")
    val currentPage: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("totalRecords")
    val totalRecords: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int
) 
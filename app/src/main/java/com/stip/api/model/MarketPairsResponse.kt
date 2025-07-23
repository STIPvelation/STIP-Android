package com.stip.stip.api.model

import com.google.gson.annotations.SerializedName
import com.stip.stip.iphome.model.IpListingItem
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Market Pairs API 응답 모델
 */
data class MarketPairsResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("countryImage")
    val countryImage: String? = null,
    
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
    
    @SerializedName("categoryId")
    val categoryId: Int? = null,
    
    @SerializedName("categoryName")
    val categoryName: String? = null,
    
    @SerializedName("lastPrice")
    val lastPrice: BigDecimal? = null, // 현재가
    
    @SerializedName("priceChange")
    val priceChange: BigDecimal? = null, // 전일대비 금액
    
    @SerializedName("changeRate")
    val changeRate: Double? = null, // 등락률 (%)
    
    @SerializedName("volume")
    val volume: BigDecimal? = null, // 거래금액(일간)
    
    @SerializedName("highTicker")
    val highTicker: BigDecimal? = null, // 최고가 (일간)
    
    @SerializedName("lowTicker")
    val lowTicker: BigDecimal? = null // 최저가 (일간)
) {
    /**
     * MarketPairsResponse를 IpListingItem으로 변환
     */
    fun toIpListingItem(): IpListingItem {
        // 공백 제거 및 데이터 정리
        val cleanBaseAsset = baseAsset.trim()
        
        return IpListingItem(
            ticker = cleanBaseAsset,
            currentPrice = String.format("%.2f", lastPrice?.toDouble() ?: 0.0),
            changePercent = String.format("%+.2f%%", changeRate ?: 0.0),
            changeAbsolute = String.format("%+.2f", priceChange?.toDouble() ?: 0.0),
            volume = String.format("%.0f", volume?.toDouble() ?: 0.0),
            category = categoryName ?: "IP",
            companyName = cleanBaseAsset,
            high24h = String.format("%.2f", highTicker?.toDouble() ?: 0.0),
            low24h = String.format("%.2f", lowTicker?.toDouble() ?: 0.0),
            volume24h = String.format("%.0f", volume?.toDouble() ?: 0.0),
            open = String.format("%.2f", lastPrice?.toDouble() ?: 0.0),
            high = String.format("%.2f", highTicker?.toDouble() ?: 0.0),
            low = String.format("%.2f", lowTicker?.toDouble() ?: 0.0),
            close = String.format("%.2f", lastPrice?.toDouble() ?: 0.0),
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
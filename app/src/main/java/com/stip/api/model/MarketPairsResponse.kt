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
    val createdAt: String
) {
    /**
     * MarketPairItem을 IpListingItem으로 변환
     */
    fun toIpListingItem(): IpListingItem {
        // 공백 제거 및 데이터 정리
        val cleanBaseAsset = baseAsset.trim()
        val cleanSymbol = symbol.trim()
        
        // TODO: 실제 데이터 변경 예정
        val basePrice = when(cleanBaseAsset) {
            "MDM" -> "1.25"
            "CDM" -> "0.95"
            "IJECT" -> "2.10"
            "WETALK" -> "1.80"
            "KOKO" -> "0.75"
            "KCOT" -> "1.50"
            "MSK" -> "3.20"
            "SMT" -> "0.85"
            "AXNO" -> "1.90"
            "KATV" -> "2.50"
            else -> "1.00"
        }
        
        // TODO: 실제 데이터 변경 예정
        val changePercent = when(cleanBaseAsset) {
            "MDM" -> "+5.2%"
            "CDM" -> "-2.1%"
            "IJECT" -> "+12.8%"
            "WETALK" -> "+7.3%"
            "KOKO" -> "-1.5%"
            "KCOT" -> "+3.7%"
            "MSK" -> "-4.2%"
            "SMT" -> "+8.9%"
            "AXNO" -> "+2.4%"
            "KATV" -> "-0.8%"
            else -> "0.0%"
        }

        // TODO: 실제 데이터 변경 예정
        val changeAbsolute = when(cleanBaseAsset) {
            "MDM" -> "+0.07"
            "CDM" -> "-0.02"
            "IJECT" -> "+0.24"
            "WETALK" -> "+0.12"
            "KOKO" -> "-0.01"
            "KCOT" -> "+0.05"
            "MSK" -> "-0.14"
            "SMT" -> "+0.07"
            "AXNO" -> "+0.04"
            "KATV" -> "-0.02"
            else -> "0.00"
        }
        
        return IpListingItem(
            ticker = cleanBaseAsset,
            currentPrice = basePrice,
            changePercent = changePercent,
            changeAbsolute = changeAbsolute,
            volume = "${(100..999).random()}K",
            category = "Patent", // 기본값
            companyName = cleanBaseAsset,
            high24h = (basePrice.toFloat() * 1.1f).toString(),
            low24h = (basePrice.toFloat() * 0.9f).toString(),
            volume24h = "${(1000..9999).random()}",
            open = (basePrice.toFloat() * 0.98f).toString(),
            high = (basePrice.toFloat() * 1.05f).toString(),
            low = (basePrice.toFloat() * 0.95f).toString(),
            close = basePrice,
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
            currentCirculation = "${(10000..99999).random()}",
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
package com.stip.ipasset.model

import com.google.gson.annotations.SerializedName

data class WalletAddressResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: WalletAddressData?
)

data class WalletAddressData(
    @SerializedName("wallet_address")
    val walletAddress: String
) 
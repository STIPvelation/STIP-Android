package com.stip.stip.order

import com.stip.stip.iphome.TradingDataHolder
import android.util.Log // Log import for debugging output

class OrderDataCoordinator(
    initialTicker: String?,
    initialBalance: Double = 10000.0,
    initialHoldings: Double = 200.50,
    private val feeRate: Double = 0.001
) {

    var currentTicker: String? = initialTicker
        private set

    var currentPrice: Float = 0.0f
        private set

    var availableUsdBalance: Double = initialBalance
        private set

    var heldAssetQuantity: Double = initialHoldings
        private set

    val userHoldsAsset: Boolean
        get() = heldAssetQuantity > 0.0

    init {
        loadDataAndSetInitialState()
    }

    fun updateTicker(newTicker: String?) {
        this.currentTicker = newTicker
        loadDataAndSetInitialState()
    }

    private fun loadDataAndSetInitialState() {
        val marketItem = TradingDataHolder.ipListingItems.firstOrNull { it.ticker == currentTicker }
        currentPrice = marketItem?.currentPrice?.replace(",", "")?.toFloatOrNull() ?: 0f
        Log.d("DataCoordinator", "Data loaded: Ticker=$currentTicker, Price=$currentPrice, Balance=$availableUsdBalance, Holdings=$heldAssetQuantity")
    }

    fun updateCurrentPrice(newPrice: Float) {
        if (newPrice >= 0f) {
            this.currentPrice = newPrice
        }
    }

    fun adjustHoldingsAfterOrder(isBuy: Boolean, quantity: Double, price: Double?) {
        Log.d("DataCoordinator","adjustHoldingsAfterOrder: Before - Balance=$availableUsdBalance, Holdings=$heldAssetQuantity")
        val executionPrice = price ?: currentPrice.toDouble()

        if (executionPrice <= 0) {
            Log.e("DataCoordinator", "Error: Invalid execution price ($executionPrice).")
            return
        }

        if (isBuy) {
            val totalCost = executionPrice * quantity
            val fee = totalCost * feeRate
            val finalCost = totalCost + fee

            availableUsdBalance = (availableUsdBalance - finalCost).coerceAtLeast(0.0)
            heldAssetQuantity += quantity
        } else {
            val totalGain = executionPrice * quantity
            val fee = totalGain * feeRate
            val finalGain = totalGain - fee

            availableUsdBalance += finalGain
            heldAssetQuantity = (heldAssetQuantity - quantity).coerceAtLeast(0.0)
        }
        Log.d("DataCoordinator","adjustHoldingsAfterOrder: After - Balance=$availableUsdBalance, Holdings=$heldAssetQuantity")
    }

    fun getActualBuyableAmount(): Double {
        return (availableUsdBalance / (1.0 + feeRate)).coerceAtLeast(0.0)
    }

    fun getActualSellableQuantity(): Double {
        return heldAssetQuantity.coerceAtLeast(0.0)
    }

    fun getAverageBuyPrice(): Double {
        return if (heldAssetQuantity > 0 && currentPrice > 0) (currentPrice * 0.98f).coerceAtLeast(0.01f).toDouble() else 0.0
    }
}
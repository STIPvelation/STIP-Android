package com.stip.stip.order

class OrderTickerManager(
    private val orderDataCoordinator: OrderDataCoordinator,
    private val updateInfoCallback: () -> Unit,
    private val updateBookCallback: () -> Unit
) {
    fun updateTicker(ticker: String?) {
        orderDataCoordinator.updateTicker(ticker)
        updateInfoCallback()
        updateBookCallback()
    }
}
package com.stip.stip.order.book

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.model.OrderBookItem // OrderBookItem import 확인
import com.stip.stip.order.adapter.OrderBookAdapter
import com.stip.stip.R
import com.stip.stip.iptransaction.api.IpTransactionService
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor

class OrderBookManager(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val orderBookAdapter: OrderBookAdapter,
    private val numberParseFormat: DecimalFormat,
    private val fixedTwoDecimalFormatter: DecimalFormat,
    private val getCurrentPrice: () -> Float,
    private val binding: FragmentOrderContentBinding,
    private val getCurrentPairId: () -> String?
) {

    private var currentAggregationLevel: Double = 0.0
    private var isTotalAmountMode: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val ORDER_BOOK_UPDATE_INTERVAL_MS = 2000L
        private val AGGREGATION_LEVEL_NAMES = mapOf(0.0 to "기본", 0.05 to "0.05", 0.1 to "0.1", 0.2 to "0.2")
        private val AGGREGATION_CYCLE_LEVELS = listOf(0.0, 0.05, 0.1, 0.2)
        private const val TAG = "OrderBookManager"
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateOrderBook()
            handler.postDelayed(this, ORDER_BOOK_UPDATE_INTERVAL_MS)
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerView.apply {
                if (layoutManager == null) {
                    layoutManager = LinearLayoutManager(context)
                }
                if (adapter == null) {
                    adapter = orderBookAdapter
                }
                setHasFixedSize(true)
                itemAnimator = null
            }
            orderBookAdapter.setDisplayMode(this.isTotalAmountMode)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    fun startAutoUpdate() {
        stopAutoUpdate()
        Log.d(TAG, "Starting auto update.")
        handler.postDelayed(updateRunnable, ORDER_BOOK_UPDATE_INTERVAL_MS)
    }


    fun updateCurrentPrice(newPrice: Float) {
        Log.d("OrderBookManager", "📌 updateCurrentPrice called with: $newPrice")
        orderBookAdapter.updateCurrentPrice(newPrice)
        triggerManualUpdate()  // 즉시 반영
    }



    fun stopAutoUpdate() {
        Log.d(TAG, "Stopping auto update.")
        handler.removeCallbacks(updateRunnable)
    }

    fun triggerManualUpdate() {
        Log.d(TAG, "Manual update triggered.")
        updateOrderBook()
    }

    fun release() {
        Log.d(TAG, "Releasing resources.")
        stopAutoUpdate()
        if (recyclerView.adapter != null) {
            recyclerView.adapter = null
        }
    }

    // --- ▼▼▼ 수정된 메서드 (호가창 API) ▼▼▼ ---
    private fun updateOrderBook() {
        try {
            val currentPrice = getCurrentPrice()
            val currentPairId = getCurrentPairId()
            
            Log.d(TAG, "Updating order book with currentPrice: $currentPrice, pairId: $currentPairId, aggregation: $currentAggregationLevel")
            
            if (currentPairId == null) {
                Log.w(TAG, "현재 pairId가 null 빈 호가창 표시")
                updateOrderBookUI(emptyList(), currentPrice)
                return
            }
            
            // 새로운 호가창 API 사용
            val orderBookRepository = com.stip.stip.api.repository.OrderBookRepository()
            
            // 코루틴 스코프에서 API 호출
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val orderBookItems = orderBookRepository.getOrderBook(currentPairId, currentPrice)
                    
                    if (orderBookItems.isNotEmpty()) {
                        Log.d(TAG, "호가창 API 호출 성공: ${orderBookItems.size}개 아이템")
                        
                        // UI 업데이트는 메인 스레드에서 실행
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            updateOrderBookUI(orderBookItems, currentPrice)
                        }
                    } else {
                        Log.w(TAG, "호가창 API 응답이 비어잇음 빈 호가창 표시")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            updateOrderBookUI(emptyList(), currentPrice)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "호가창 API 호출 실패", e)
                    // 실패 시 빈 호가창 표시
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        updateOrderBookUI(emptyList(), currentPrice)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order book", e)
            // 예외 발생 시 빈 호가창 표시
            val currentPrice = getCurrentPrice()
            updateOrderBookUI(emptyList(), currentPrice)
        }
    }

    private fun updateOrderBookUI(rawList: List<OrderBookItem>, currentPrice: Float) {
        val aggregatedList = if (this.currentAggregationLevel > 0.0) {
            generateAggregatedOrderBook(rawList, this.currentAggregationLevel)
        } else {
            rawList
        }

        val sortedSells = aggregatedList.filter { !it.isBuy && !it.isGap }.sortedByDescending { parseDouble(it.price) }
        val sortedBuys = aggregatedList.filter { it.isBuy && !it.isGap }.sortedByDescending { parseDouble(it.price) }

        // 시장가 주문 Radio 비활성화 로직
        // 매수 탭: 매도(asks)가 0개면 비활성화, 매도 탭: 매수(bids)가 0개면 비활성화 (터치해도 시장 주문으로 변하지 않음)
        val tabPosition = binding.tabLayoutOrderMode.selectedTabPosition
        val radioMarket = binding.radioMarketOrder
        if (tabPosition == 0) { // 매수 탭
            radioMarket.isEnabled = sortedSells.isNotEmpty()
        } else if (tabPosition == 1) { // 매도 탭
            radioMarket.isEnabled = sortedBuys.isNotEmpty()
        }

        val gapItem = OrderBookItem(price = "", quantity = "", isBuy = false, percent = "", isGap = true)

        // 매도/매수 각각 30개로 고정 패딩, isBuy 명확히
        val FIXED_SELL_SIZE = 30
        val FIXED_BUY_SIZE = 30

        fun topPad(list: List<OrderBookItem>, total: Int, isBuy: Boolean): List<OrderBookItem> {
            val padSize = total - list.size
            return List(padSize) { OrderBookItem(isBuy = isBuy) } + list
        }
        fun bottomPad(list: List<OrderBookItem>, total: Int, isBuy: Boolean): List<OrderBookItem> {
            val padSize = total - list.size
            return list + List(padSize) { OrderBookItem(isBuy = isBuy) }
        }

        val paddedSells = topPad(sortedSells, FIXED_SELL_SIZE, false)
        val paddedBuys = bottomPad(sortedBuys, FIXED_BUY_SIZE, true)

        val listWithGap = mutableListOf<OrderBookItem>().apply {
            addAll(paddedSells)
            add(gapItem)
            addAll(paddedBuys)
        }

        Log.d(TAG, "Final order book size: ${listWithGap.size} (Sells: ${paddedSells.size}, Buys: ${paddedBuys.size})")
        orderBookAdapter.updateData(listWithGap, currentPrice)
    }

    private fun scrollToCenter() {
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@post
            val adapter = recyclerView.adapter as? OrderBookAdapter ?: return@post
            val height = recyclerView.height
            if (height <= 0 || adapter.itemCount <= 0) {
                Log.w(TAG,"Cannot scroll, RV height=$height, itemCount=${adapter.itemCount}")
                return@post
            }

            val firstBuyIndex = adapter.getFirstBuyOrderIndex()
            Log.d(TAG, "Scrolling to center. Height=$height, Count=${adapter.itemCount}, FirstBuyIndex(GapIndex+1)=$firstBuyIndex")

            try {
                if (firstBuyIndex >= 0 && firstBuyIndex < adapter.itemCount) {
                    val offset = height / 2 - (recyclerView.findViewHolderForAdapterPosition(firstBuyIndex)?.itemView?.height ?: 30) / 2
                    layoutManager.scrollToPositionWithOffset(firstBuyIndex, offset)
                } else if (adapter.itemCount > 0) {
                    layoutManager.scrollToPositionWithOffset(adapter.itemCount / 2, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during scrolling", e)
            }
        }
    }

    fun setupBottomOptionListeners() {
        Log.d(TAG, "Setting up bottom option listeners.")
        updateQuantityTotalTextView()
        updateAggregationButtonText()

        binding.optionViewAllContainer.setOnClickListener {
            val currentIndex = AGGREGATION_CYCLE_LEVELS.indexOf(this.currentAggregationLevel)
            val nextIndex = (currentIndex + 1) % AGGREGATION_CYCLE_LEVELS.size
            this.currentAggregationLevel = AGGREGATION_CYCLE_LEVELS[nextIndex]
            Log.d(TAG, "Aggregation level changed to: ${this.currentAggregationLevel}")
            updateAggregationButtonText()
            triggerManualUpdate()
        }

        binding.optionQuantityTotalToggleContainer.setOnClickListener {
            this.isTotalAmountMode = !this.isTotalAmountMode
            Log.d(TAG, "Total amount mode toggled: ${this.isTotalAmountMode}")
            updateQuantityTotalTextView()
            orderBookAdapter.setDisplayMode(this.isTotalAmountMode)
            triggerManualUpdate()
        }
    }

    private fun updateQuantityTotalTextView() {
        binding.textOptionQuantityTotalToggle.text = context.getString(
            if (this.isTotalAmountMode)
                R.string.total_amount_label
            else
                R.string.quantity_label
        )
    }

    private fun updateAggregationButtonText() {
        val levelText = AGGREGATION_LEVEL_NAMES[this.currentAggregationLevel]
            ?: String.format(Locale.US, "%.2f", this.currentAggregationLevel)

        binding.textOptionViewAll.text = if (this.currentAggregationLevel == 0.0) {
            context.getString(R.string.gather_the_price)
        } else {
            context.getString(R.string.gather_the_price) + " ($levelText)"
        }
    }

    // 더미 데이터 생성 함수 - 사용하지 않음
    /*
    fun generateDummyOrderBook(
        currentPrice: Float,
        fixedTwoDecimalFormatter: DecimalFormat,
        numberParseFormat: DecimalFormat
    ): List<OrderBookItem> {
        if (currentPrice <= 0f) return emptyList()
        val sellOrders = mutableListOf<OrderBookItem>()
        val buyOrders = mutableListOf<OrderBookItem>()
        val step = when {
            currentPrice < 1f -> 0.001f; currentPrice < 10f -> 0.01f
            currentPrice < 100f -> 0.1f; currentPrice < 1000f -> 0.5f
            else -> 1.0f
        }.coerceAtLeast(0.001f)
        val numOrdersPerSide = 30
        val random = Random()

        for (i in numOrdersPerSide downTo 1) {
            val price = currentPrice + i * step
            val quantity = random.nextDouble() * 50 + 10
            val percent = if (currentPrice > 0) ((price - currentPrice) / currentPrice) * 100 else 0.0
            sellOrders.add(
                OrderBookItem(
                    price = fixedTwoDecimalFormatter.format(price.toDouble()),
                    percent = String.format(Locale.US, "+%.2f%%", percent),
                    quantity = fixedTwoDecimalFormatter.format(quantity),
                    isBuy = false
                )
            )
        }
        for (i in 1..numOrdersPerSide) {
            val price = (currentPrice - i * step).coerceAtLeast(step)
            if (price <= 0) continue
            val quantity = random.nextDouble() * 40 + 8
            val percent = if (currentPrice > 0) ((price - currentPrice) / currentPrice) * 100 else 0.0
            buyOrders.add(
                OrderBookItem(
                    price = fixedTwoDecimalFormatter.format(price.toDouble()),
                    percent = String.format(Locale.US, "%.2f%%", percent),
                    quantity = fixedTwoDecimalFormatter.format(quantity),
                    isBuy = true
                )
            )
        }
        return sellOrders + buyOrders
    }
    */

    fun generateAggregatedOrderBook(
        baseData: List<OrderBookItem>,
        aggregationStep: Double
    ): List<OrderBookItem> {
        if (aggregationStep <= 0.0) return baseData

        fun parseDoubleLocal(value: String?): Double {
            return try {
                if (value.isNullOrBlank()) 0.0 else numberParseFormat.parse(value)?.toDouble() ?: 0.0
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse double in aggregate: $value", e)
                0.0
            }
        }

        fun aggregate(list: List<OrderBookItem>, isBuy: Boolean): List<OrderBookItem> {
            return list.filter { it.isBuy == isBuy && !it.isGap }.groupBy { item ->
                floor(parseDoubleLocal(item.price) / aggregationStep) * aggregationStep
            }.mapNotNull { (keyPrice, group) ->
                if (keyPrice <= 0) return@mapNotNull null
                val totalQuantity = group.sumOf { parseDoubleLocal(it.quantity) }
                if (totalQuantity <= 0) return@mapNotNull null

                OrderBookItem(
                    price = fixedTwoDecimalFormatter.format(keyPrice),
                    quantity = fixedTwoDecimalFormatter.format(totalQuantity),
                    isBuy = isBuy,
                    percent = ""
                )
            }
        }

        val aggregatedSells = aggregate(baseData, false)
        val aggregatedBuys = aggregate(baseData, true)

        return aggregatedSells + aggregatedBuys
    }

    fun initializeAndStart() {
        Log.d(TAG, "Initializing Order Book...")
        setupRecyclerView()
        updateOrderBook()
        recyclerView.post { scrollToCenter() }
    }

    private fun parseDouble(value: String?): Double {
        return try {
            if (value.isNullOrBlank()) 0.0 else numberParseFormat.parse(value)?.toDouble() ?: 0.0
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse double for sorting: '$value'")
            0.0
        }
    }
}
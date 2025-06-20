package com.stip.stip.order

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.OrderBookAdapter
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.model.OrderBookItem // OrderBookItem import 확인
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor
import com.stip.stip.R


class OrderBookManager(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val orderBookAdapter: OrderBookAdapter,
    private val numberParseFormat: DecimalFormat,
    private val fixedTwoDecimalFormatter: DecimalFormat,
    private val getCurrentPrice: () -> Float,
    private val binding: FragmentOrderContentBinding
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

    // --- ▼▼▼ 수정된 메서드 (Gap 아이템 추가 로직 포함) ▼▼▼ ---
    private fun updateOrderBook() {
        try {
            val currentPrice = getCurrentPrice()
            if (currentPrice <= 0f) {
                Log.w(TAG, "Current price is invalid ($currentPrice), submitting empty list.")
                orderBookAdapter.updateData(emptyList(), 0f)
                return
            }

            Log.d(TAG, "Updating order book with currentPrice: $currentPrice, aggregation: $currentAggregationLevel")
            val rawList = generateDummyOrderBook(currentPrice, this.fixedTwoDecimalFormatter, this.numberParseFormat)
            val aggregatedList = if (this.currentAggregationLevel > 0.0) {
                generateAggregatedOrderBook(rawList, this.currentAggregationLevel)
            } else {
                rawList
            }

            val sortedSells = aggregatedList.filter { !it.isBuy && !it.isGap }.sortedByDescending { parseDouble(it.price) }
            val sortedBuys = aggregatedList.filter { it.isBuy && !it.isGap }.sortedByDescending { parseDouble(it.price) }

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

            // --- ▼▼▼ 로그 추가 ▼▼▼ ---
            Log.d(TAG, "List being sent to adapter (${listWithGap.size} items):")
            listWithGap.forEachIndexed { index, item ->
                val type = when {
                    item.isGap -> "GAP"
                    item.isBuy -> "BUY"
                    else -> "SELL"
                }
                Log.d(TAG, "  [$index] Type: $type, Price: ${item.price}, Qty: ${item.quantity}")
            }
            // --- ▲▲▲ 로그 추가 ▲▲▲ ---

            orderBookAdapter.updateData(listWithGap, currentPrice)
            // 항상 gap(중앙) 인덱스를 기준으로 중앙 스크롤
            recyclerView.post { scrollToCenter() }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating order book data", e)
            try {
                orderBookAdapter.updateData(emptyList(), getCurrentPrice())
            } catch (adapterError: Exception) {
                Log.e(TAG, "Error clearing adapter data", adapterError)
            }
        }
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

            // getFirstBuyOrderIndex 는 이제 Gap 아이템 인덱스를 반환할 수 있음
            val firstBuyIndex = adapter.getFirstBuyOrderIndex()
            Log.d(TAG, "Scrolling to center. Height=$height, Count=${adapter.itemCount}, FirstBuyIndex(GapIndex+1)=$firstBuyIndex")

            try {
                // Gap 아이템 또는 그 직전/직후 아이템을 중앙으로 스크롤
                // getFirstBuyOrderIndex가 정확히 gap 다음의 첫 buy 아이템 인덱스를 반환한다고 가정
                if (firstBuyIndex >= 0 && firstBuyIndex < adapter.itemCount) {
                    // 첫 매수 아이템이 화면 중앙보다 약간 아래에 오도록 조정 (선택 사항)
                    val offset = height / 2 - (recyclerView.findViewHolderForAdapterPosition(firstBuyIndex)?.itemView?.height ?: 30) / 2
                    layoutManager.scrollToPositionWithOffset(firstBuyIndex, offset)
                } else if (adapter.itemCount > 0) {
                    // 매수 아이템이 없거나 찾을 수 없을 때 중간으로 이동
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
            // 모드 변경 시 ProgressBar 스케일 재계산을 위해 수동 업데이트 트리거
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
        // 정렬은 updateOrderBook 에서 최종적으로 수행하므로 여기서는 제거 가능 (또는 유지)
        return sellOrders + buyOrders
    }

    fun generateAggregatedOrderBook(
        baseData: List<OrderBookItem>,
        aggregationStep: Double
    ): List<OrderBookItem> {
        if (aggregationStep <= 0.0) return baseData

        fun parseDoubleLocal(value: String?): Double { // Renamed to avoid conflict if parseDouble is top-level
            return try {
                if (value.isNullOrBlank()) 0.0 else numberParseFormat.parse(value)?.toDouble() ?: 0.0
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse double in aggregate: $value", e)
                0.0
            }
        }

        fun aggregate(list: List<OrderBookItem>, isBuy: Boolean): List<OrderBookItem> {
            return list.filter { it.isBuy == isBuy && !it.isGap }.groupBy { item -> // isGap 필터 추가
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
            } // 정렬은 updateOrderBook 에서 최종적으로 수행
        }

        val aggregatedSells = aggregate(baseData, false)
        val aggregatedBuys = aggregate(baseData, true)

        return aggregatedSells + aggregatedBuys
    }


    fun initializeAndStart() {
        Log.d(TAG, "Initializing Order Book...")
        setupRecyclerView()
        updateOrderBook() // 초기 데이터 로드 및 어댑터 업데이트
        recyclerView.post { scrollToCenter() } // 데이터 설정 후 스크롤
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
package com.stip.stip.order.adapter

import android.animation.ObjectAnimator
import android.graphics.Color // Color import
import android.graphics.drawable.ColorDrawable // ColorDrawable import
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.iphome.fragment.OnOrderBookItemClickListener
import com.stip.stip.iphome.model.OrderBookItem
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

class OrderBookAdapter(
    private var currentPrice: Float,
    private val listener: OnOrderBookItemClickListener
) : ListAdapter<OrderBookItem, RecyclerView.ViewHolder>(OrderBookDiffCallback()) {

    private var openPrice: Float = 0f
    private var maxValueForScale: Float = 1f
    private var currentDisplayModeIsTotalAmount: Boolean = false
    private var highlightedPosition: Int = -1

    private val twoDecimalFormatter = DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private val numberParseFormat = DecimalFormat.getNumberInstance(Locale.US)
    private var borderDrawable: Drawable? = null
    private var defaultBackground: Drawable? = null

    companion object {
        private const val TAG = "OrderBookAdapter"
        private const val VIEW_TYPE_SELL = 0
        private const val VIEW_TYPE_BUY = 1
    }

    fun updateOpenPrice(newOpenPrice: Float) {
        this.openPrice = newOpenPrice
        notifyDataSetChanged()
    }

    fun updateData(newList: List<OrderBookItem>, newCurrentPrice: Float) {
        Log.d(TAG, "🔁 updateData called with newCurrentPrice = $newCurrentPrice")

        val oldCurrentPrice = this.currentPrice
        this.currentPrice = newCurrentPrice
        this.maxValueForScale = calculateMaxValue(newList.filter { !it.isGap }, currentDisplayModeIsTotalAmount)

        // 간격 아이템 제거 - 실제 호가만 표시
        val listWithoutGap = newList.filter { !it.isGap }

        // 현재 가격과 가장 가까운 행 찾기
        var closestPosition = -1
        var minDifference = Float.MAX_VALUE
        var exactMatchPosition = -1
        
        listWithoutGap.forEachIndexed { index, item ->
            try {
                val itemPrice = numberParseFormat.parse(item.price)?.toFloat() ?: 0f
                if (itemPrice > 0) {
                    // 정확히 일치하는 경우 우선 저장
                    if (itemPrice == newCurrentPrice) {
                        exactMatchPosition = index
                    }
                    
                    val difference = kotlin.math.abs(itemPrice - newCurrentPrice)
                    if (difference < minDifference) {
                        minDifference = difference
                        closestPosition = index
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "가격 파싱 하이라이트 에러", e)
            }
        }
        
        val oldHighlightedPosition = highlightedPosition
        // 정확히 일치하는 호가가 있으면 그것을 사용, 없으면 가장 가까운 호가 사용
        highlightedPosition = if (exactMatchPosition != -1) exactMatchPosition else closestPosition

        submitList(listWithoutGap)
        Log.d(TAG, "✅ Final list submitted. Size: ${listWithoutGap.size}, MaxValue: $maxValueForScale")
    }

    fun updateCurrentPrice(newPrice: Float) {
        this.currentPrice = newPrice
        
        // 현재 리스트에서 현재가와 가장 가까운 위치 다시 계산
        val currentList = currentList.filter { !it.isGap }
        var closestPosition = -1
        var minDifference = Float.MAX_VALUE
        var exactMatchPosition = -1
        
        currentList.forEachIndexed { index, item ->
            try {
                val itemPrice = numberParseFormat.parse(item.price)?.toFloat() ?: 0f
                if (itemPrice > 0) {
                    // 정확히 일치하는 경우 우선 저장
                    if (itemPrice == newPrice) {
                        exactMatchPosition = index
                    }
                    
                    val difference = kotlin.math.abs(itemPrice - newPrice)
                    if (difference < minDifference) {
                        minDifference = difference
                        closestPosition = index
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "가격 파싱 하이라이트 에러", e)
            }
        }
        
        val oldHighlightedPosition = highlightedPosition
        // 정확히 일치하는 호가가 있으면 그것을 사용, 없으면 가장 가까운 호가 사용
        highlightedPosition = if (exactMatchPosition != -1) exactMatchPosition else closestPosition
        
        // 하이라이트가 변경된 경우에만 해당 아이템들 업데이트
        if (oldHighlightedPosition != highlightedPosition) {
            if (oldHighlightedPosition >= 0 && oldHighlightedPosition < itemCount) {
                notifyItemChanged(oldHighlightedPosition)
            }
            if (highlightedPosition >= 0 && highlightedPosition < itemCount) {
                notifyItemChanged(highlightedPosition)
            }
        } else {
            // 하이라이트 위치가 같아도 현재가가 변경되었으므로 전체 업데이트
            notifyDataSetChanged()
        }
    }

    fun setDisplayMode(isTotalAmount: Boolean) {
        if (currentDisplayModeIsTotalAmount != isTotalAmount) {
            currentDisplayModeIsTotalAmount = isTotalAmount
            this.maxValueForScale = calculateMaxValue(
                currentList.filter { !it.isGap },
                currentDisplayModeIsTotalAmount
            )
            notifyItemRangeChanged(0, itemCount)
        }
    }

    fun getFirstBuyOrderIndex(): Int {
        return currentList.indexOfFirst { it.isBuy }
    }

    // ⛔️ 반드시 아래에 DiffUtil 정의가 있어야 함!
    private class OrderBookDiffCallback : DiffUtil.ItemCallback<OrderBookItem>() {
        override fun areItemsTheSame(oldItem: OrderBookItem, newItem: OrderBookItem): Boolean {
            return oldItem.price == newItem.price && oldItem.isBuy == newItem.isBuy && oldItem.isGap == newItem.isGap
        }

        override fun areContentsTheSame(oldItem: OrderBookItem, newItem: OrderBookItem): Boolean {
            return oldItem == newItem
        }
    }

    private fun calculateMaxValue(list: List<OrderBookItem>, isTotalAmountMode: Boolean): Float {
        val maxValue = list.mapNotNull { item ->
            if (item.isGap || item.price.isBlank() || item.price == "--" || item.quantity.isBlank()) return@mapNotNull null
            try {
                val quantity = numberParseFormat.parse(item.quantity)?.toFloat() ?: 0f
                if (isTotalAmountMode) {
                    val price = numberParseFormat.parse(item.price)?.toFloat() ?: 0f
                    quantity * price
                } else {
                    quantity
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error parsing value for max calc: Q=${item.quantity}, P=${item.price}",
                    e
                )
                null
            }
        }.maxOrNull() ?: 1f
        return if (maxValue <= 0f) 1f else maxValue
    }

    override fun getItemViewType(position: Int): Int {
        return try {
            val item = getItem(position)
            when {
                !item.isBuy -> VIEW_TYPE_SELL
                else -> VIEW_TYPE_BUY
            }
        } catch (e: Exception) {
            Log.e(TAG, "IndexOutOfBounds in getItemViewType for position $position", e)
            -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        // 현재가 테두리 Drawable
        if (borderDrawable == null) {
            borderDrawable = ContextCompat.getDrawable(
                parent.context,
                R.drawable.bg_border_black
            )
        }

        // 매수/매도 항목의 기본 배경 캐싱
        if (defaultBackground == null) {
            val layoutId = if (viewType == VIEW_TYPE_SELL) {
                R.layout.item_order_book_sell
            } else {
                R.layout.item_order_book_buy
            }
            val tempView = inflater.inflate(layoutId, parent, false)
            defaultBackground = tempView.background ?: ColorDrawable(Color.TRANSPARENT)
        }

        return when (viewType) {
            VIEW_TYPE_SELL -> {
                val view = inflater.inflate(R.layout.item_order_book_sell, parent, false)
                OrderBookViewHolder(view, listener) { pos -> getItem(pos) }
            }

            VIEW_TYPE_BUY -> {
                val view = inflater.inflate(R.layout.item_order_book_buy, parent, false)
                OrderBookViewHolder(view, listener) { pos -> getItem(pos) }
            }

            else -> {
                Log.e(TAG, "Invalid viewType in onCreateViewHolder: $viewType")
                val emptyView = View(parent.context)
                object : RecyclerView.ViewHolder(emptyView) {}
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val item = getItem(position)

            // ✅ 현재가 기준 시장 방향 판단 (상승/하락)
            val isMarketUp = currentPrice >= openPrice
            val textColorResId = if (isMarketUp) {
                R.color.percentage_positive_red
            } else {
                R.color.percentage_negative_blue
            }

            when (holder) {
                is OrderBookViewHolder -> {
                    holder.bind(
                        item = item,
                        currentPrice = currentPrice,
                        maxValueForScale = maxValueForScale,
                        displayModeIsTotalAmount = currentDisplayModeIsTotalAmount,
                        formatter = twoDecimalFormatter,
                        borderDrawable = borderDrawable,
                        defaultBackground = defaultBackground,
                        openPrice = openPrice,
                        fixedTextColorResId = textColorResId,
                        highlightedPosition = highlightedPosition
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }



    class OrderBookViewHolder(
        itemView: View,
        private val listener: OnOrderBookItemClickListener,
        private val getItemForPosition: (Int) -> OrderBookItem?
    ) : RecyclerView.ViewHolder(itemView) {

        private val priceText: TextView? = itemView.findViewById(R.id.text_order_price_v1)
            ?: itemView.findViewById(R.id.text_order_price_v2)
        private val percentText: TextView? = itemView.findViewById(R.id.text_order_percentage_v1)
            ?: itemView.findViewById(R.id.text_order_percentage_v2)
        private val quantityOrTotalText: TextView? =
            itemView.findViewById(R.id.text_order_quantity_v1)
                ?: itemView.findViewById(R.id.text_order_quantity_v2)
        private val progressBar: ProgressBar? = itemView.findViewById(R.id.quantity_progress_bar)
        private val numberParser = DecimalFormat.getNumberInstance(Locale.US)

        init {
            itemView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    getItemForPosition(currentPosition)?.let { item ->
                        if (!item.isGap && item.price.isNotEmpty() && item.price != "--") {
                            listener.onPriceClicked(item.price)
                        }
                    }
                }
            }
        }

        fun bind(
            item: OrderBookItem,
            currentPrice: Float,
            maxValueForScale: Float,
            displayModeIsTotalAmount: Boolean,
            formatter: DecimalFormat,
            borderDrawable: Drawable?,
            defaultBackground: Drawable?,
            openPrice: Float,
            fixedTextColorResId: Int, // ✅ 외부에서 강제 지정된 색상
            highlightedPosition: Int
        ) {
            itemView.visibility = View.VISIBLE

            if (item.price.isBlank() || item.price == "--") {
                // 빈 아이템은 내용만 숨기고 공간은 유지
                priceText?.text = ""
                percentText?.text = ""
                quantityOrTotalText?.text = ""
                progressBar?.progress = 0
                itemView.visibility = View.VISIBLE
                itemView.background = defaultBackground
                return
            }

            // 🔢 숫자 파싱
            val priceFloat = try {
                val parsedPrice = numberParser.parse(item.price)?.toFloat() ?: currentPrice
                parsedPrice
            } catch (e: Exception) {
                Log.e(TAG, "price 파싱 에러: '${item.price}'", e)
                currentPrice
            }

            val quantityFloat = try {
                numberParser.parse(item.quantity)?.toFloat() ?: 0f
            } catch (e: Exception) {
                0f
            }

            priceText?.text = formatter.format(priceFloat)

            val percentValue = if (currentPrice > 0f)
                ((priceFloat - currentPrice) / currentPrice) * 100
            else 0f
            percentText?.text = String.format(Locale.US, "%+.2f%%", percentValue)

            val valueForProgressBar = if (displayModeIsTotalAmount) {
                val total = priceFloat * quantityFloat
                quantityOrTotalText?.text = formatter.format(total)
                total
            } else {
                quantityOrTotalText?.text = formatter.format(quantityFloat)
                quantityFloat
            }

            val textColor = ContextCompat.getColor(itemView.context, fixedTextColorResId)
            priceText?.setTextColor(textColor)
            percentText?.setTextColor(textColor)

            // 현재가 강조 표시: 가장 가까운 하나의 행에만 테두리 적용
            val shouldHighlight = bindingAdapterPosition == highlightedPosition
            
            if (shouldHighlight) {
                Log.d(TAG, "Highlighting position $bindingAdapterPosition with price: $priceFloat (current: $currentPrice)")
                // 검은색 테두리 적용
                itemView.background = borderDrawable
                // 테두리가 잘 보이도록 패딩 추가
                itemView.setPadding(4, 4, 4, 4)
            } else {
                itemView.background = defaultBackground
                // 기본 패딩으로 되돌림
                itemView.setPadding(0, 0, 0, 0)
            }

            val progressDrawableRes = when {
                item.isCurrentPrice -> R.drawable.progress_bar_current
                item.isBuy -> R.drawable.progress_bar_buy
                else -> R.drawable.progress_bar_sell
            }
            progressBar?.progressDrawable =
                ContextCompat.getDrawable(itemView.context, progressDrawableRes)

            progressBar?.apply {
                val progressPercent = if (maxValueForScale > 0f)
                    ((valueForProgressBar / maxValueForScale) * 100).toInt().coerceIn(0, 100)
                else 0

                progressTintList = null
                indeterminateTintList = null

                val currentProgressAnim = this.progress
                if (isAttachedToWindow && currentProgressAnim != progressPercent) {
                    ObjectAnimator.ofInt(this, "progress", currentProgressAnim, progressPercent).apply {
                        duration = 300
                        interpolator = DecelerateInterpolator()
                    }.start()
                } else {
                    this.progress = progressPercent
                }
            }
        }
    }
}

package com.stip.stip

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
        private const val VIEW_TYPE_GAP = 2
        private const val PRICE_THRESHOLD_FACTOR = 0.0001f
    }

    fun updateOpenPrice(newOpenPrice: Float) {
        this.openPrice = newOpenPrice
        notifyDataSetChanged()
    }

    fun updateData(newList: List<OrderBookItem>, newCurrentPrice: Float) {
        Log.d(TAG, "🔁 updateData called with newCurrentPrice = $newCurrentPrice")

        this.currentPrice = newCurrentPrice
        this.maxValueForScale = calculateMaxValue(newList.filter { !it.isGap }, currentDisplayModeIsTotalAmount)

        val closestSell = newList.filter { !it.isBuy && !it.isGap }
            .minByOrNull {
                try {
                    abs(it.price.toFloat() - newCurrentPrice)
                } catch (e: Exception) {
                    Float.MAX_VALUE
                }
            }

        val closestBuy = newList.filter { it.isBuy && !it.isGap }
            .minByOrNull {
                try {
                    abs(it.price.toFloat() - newCurrentPrice)
                } catch (e: Exception) {
                    Float.MAX_VALUE
                }
            }

        val sellDiff = try {
            abs((closestSell?.price?.toFloat() ?: Float.MAX_VALUE) - newCurrentPrice)
        } catch (e: Exception) {
            Float.MAX_VALUE
        }

        val buyDiff = try {
            abs((closestBuy?.price?.toFloat() ?: Float.MAX_VALUE) - newCurrentPrice)
        } catch (e: Exception) {
            Float.MAX_VALUE
        }

        val isBuySide = buyDiff < sellDiff
        val quantity = if (isBuySide) {
            closestBuy?.quantity ?: "0.00"
        } else {
            closestSell?.quantity ?: "0.00"
        }

        val currentPriceItem = OrderBookItem(
            price = newCurrentPrice.toString(),
            quantity = quantity,
            isBuy = isBuySide,
            isCurrentPrice = true,
            isGap = true
        )

        val gapIndex = newList.indexOfFirst { it.isGap }
        val combinedList = if (gapIndex != -1) {
            newList.toMutableList().apply {
                this[gapIndex] = currentPriceItem
            }
        } else {
            newList.toMutableList().apply {
                add(newList.size / 2, currentPriceItem)
            }
        }

        submitList(combinedList)
        Log.d(TAG, "✅ Final list submitted. Size: ${combinedList.size}, MaxValue: $maxValueForScale")
    }

    fun updateCurrentPrice(newPrice: Float) {
        this.currentPrice = newPrice
        notifyDataSetChanged()
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
        val gapIndex = currentList.indexOfFirst { it.isGap }
        return if (gapIndex != -1 && gapIndex + 1 < currentList.size) gapIndex + 1 else gapIndex
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
                item.isGap -> VIEW_TYPE_GAP
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

        // 매수/매도 항목의 기본 배경 캐싱 (현재가 제외)
        if (defaultBackground == null && viewType != VIEW_TYPE_GAP) {
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

            VIEW_TYPE_GAP -> {
                val view = inflater.inflate(R.layout.item_order_book_current_price, parent, false)
                GapViewHolder(view)
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
                    if (!item.isGap) {
                        holder.bind(
                            item = item,
                            currentPrice = currentPrice,
                            maxValueForScale = maxValueForScale,
                            displayModeIsTotalAmount = currentDisplayModeIsTotalAmount,
                            formatter = twoDecimalFormatter,
                            borderDrawable = borderDrawable,
                            defaultBackground = defaultBackground,
                            openPrice = openPrice, // 그대로 전달
                            fixedTextColorResId = textColorResId  // ✅ 색상 강제 지정
                        )
                    } else {
                        holder.itemView.visibility = View.GONE
                    }
                }

                is GapViewHolder -> {
                    val context = holder.itemView.context
                    val priceText = holder.itemView.findViewById<TextView>(R.id.text_order_price_current)
                    val percentText = holder.itemView.findViewById<TextView>(R.id.text_order_percentage_current)
                    val quantityText = holder.itemView.findViewById<TextView>(R.id.text_order_quantity_current)

                    val priceFloat = try {
                        numberParseFormat.parse(item.price)?.toFloat() ?: currentPrice
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse price for currentPrice item", e)
                        currentPrice
                    }

                    val quantityFloat = try {
                        numberParseFormat.parse(item.quantity)?.toFloat() ?: 0f
                    } catch (e: Exception) {
                        0f
                    }

                    val percentValue = if (currentPrice > 0f) {
                        ((priceFloat - currentPrice) / currentPrice) * 100f
                    } else 0f

                    val percentStr = String.format(Locale.US, "%+.2f%%", percentValue)

                    val textColor = ContextCompat.getColor(context, textColorResId)

                    priceText?.apply {
                        text = twoDecimalFormatter.format(priceFloat)
                        setTextColor(textColor)
                    }

                    percentText?.apply {
                        text = percentStr
                        setTextColor(textColor)
                    }

                    quantityText?.apply {
                        text = twoDecimalFormatter.format(quantityFloat)
                        setTextColor(Color.BLACK)
                    }

                    holder.itemView.setBackgroundResource(R.drawable.bg_border_black)
                    holder.itemView.visibility = View.VISIBLE

                    holder.itemView.setOnClickListener {
                        if (item.price.isNotBlank()) {
                            listener.onPriceClicked(item.price)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }

    class GapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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
            fixedTextColorResId: Int // ✅ 외부에서 강제 지정된 색상
        ) {
            itemView.visibility = View.VISIBLE

            if (item.price.isBlank() || item.price == "--") {
                priceText?.text = ""
                percentText?.text = ""
                quantityOrTotalText?.text = ""
                progressBar?.progress = 0

                val secondaryColor = ContextCompat.getColor(itemView.context, R.color.text_secondary)
                priceText?.setTextColor(secondaryColor)
                percentText?.setTextColor(secondaryColor)
                quantityOrTotalText?.setTextColor(secondaryColor)
                itemView.background = defaultBackground
                return
            }

            // 🔢 숫자 파싱
            val priceFloat = try {
                numberParser.parse(item.price)?.toFloat() ?: currentPrice
            } catch (e: Exception) {
                currentPrice
            }

            val quantityFloat = try {
                numberParser.parse(item.quantity)?.toFloat() ?: 0f
            } catch (e: Exception) {
                0f
            }

            // 💬 가격 & 퍼센트 텍스트 표시
            priceText?.text = formatter.format(priceFloat)

            val percentValue = if (currentPrice > 0f)
                ((priceFloat - currentPrice) / currentPrice) * 100
            else 0f
            percentText?.text = String.format(Locale.US, "%+.2f%%", percentValue)

            // 📊 수량 또는 금액 표시
            val valueForProgressBar = if (displayModeIsTotalAmount) {
                val total = priceFloat * quantityFloat
                quantityOrTotalText?.text = formatter.format(total)
                total
            } else {
                quantityOrTotalText?.text = formatter.format(quantityFloat)
                quantityFloat
            }

            // 🎨 색상 강제 지정 (현재가 >= 시작가 → 빨간색, 아니면 파란색)
            val textColor = ContextCompat.getColor(itemView.context, fixedTextColorResId)
            priceText?.setTextColor(textColor)
            percentText?.setTextColor(textColor)

            // 🔲 현재가 테두리
            val priceDifference = abs(priceFloat - currentPrice)
            val threshold = currentPrice * PRICE_THRESHOLD_FACTOR
            itemView.background = if (priceFloat > 0 && priceDifference < threshold)
                borderDrawable else defaultBackground

            // 📉 ProgressBar 스타일 및 애니메이션
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


        private class OrderBookDiffCallback : DiffUtil.ItemCallback<OrderBookItem>() {
            override fun areItemsTheSame(oldItem: OrderBookItem, newItem: OrderBookItem): Boolean {
                return if (oldItem.isGap != newItem.isGap) {
                    false
                } else if (oldItem.isGap) {
                    true
                } else {
                    oldItem.price == newItem.price && oldItem.isBuy == newItem.isBuy
                }
            }

            override fun areContentsTheSame(
                oldItem: OrderBookItem,
                newItem: OrderBookItem
            ): Boolean {
                return if (oldItem.isGap) newItem.isGap else oldItem == newItem
            }
        }
    }
}
package com.stip.stip.iphome.adapter

// 파일 맨 위에 아래를 추가하세요
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.iphome.model.OrderBookItem
import android.widget.TextView
import androidx.core.content.ContextCompat


class OrderBookAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<OrderBookItem>()

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when {
            item.isCurrentPrice -> VIEW_TYPE_CURRENT_PRICE
            item.isGap -> VIEW_TYPE_GAP
            item.isBuy -> VIEW_TYPE_BUY
            else -> VIEW_TYPE_SELL
        }
    }

    companion object {
        private const val VIEW_TYPE_SELL = 0
        private const val VIEW_TYPE_CURRENT_PRICE = 1
        private const val VIEW_TYPE_BUY = 2
        private const val VIEW_TYPE_GAP = 3
    }

    class SellViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: OrderBookItem) {
            // 매도 항목을 위한 뷰 바인딩
        }
    }

    class BuyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: OrderBookItem) {
            // 매수 항목을 위한 뷰 바인딩
        }
    }

    class CurrentPriceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val currentPriceTextView: TextView = view.findViewById(R.id.text_order_price_current)

        fun bind(item: OrderBookItem) {
            // 현재가 값 설정
            currentPriceTextView.text = item.price

            // 현재가 항목에 테두리 설정
            val context = itemView.context
            val drawable = ContextCompat.getDrawable(context, R.drawable.border_current_price) // 테두리 리소스를 drawable로 설정

            
        }
    }

    class GapViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            // 빈 줄 또는 구분선
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SELL -> SellViewHolder(inflater.inflate(R.layout.item_order_book_sell, parent, false))
            VIEW_TYPE_BUY -> BuyViewHolder(inflater.inflate(R.layout.item_order_book_buy, parent, false))
            VIEW_TYPE_CURRENT_PRICE -> CurrentPriceViewHolder(inflater.inflate(R.layout.item_order_book_current_price, parent, false))
            VIEW_TYPE_GAP -> GapViewHolder(inflater.inflate(R.layout.item_order_book_gap, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        // 각 아이템의 시작가/어제 가격 비교 후 배경색 결정
        val isUp = item.startPrice > item.yesterdayPrice
        val bgColorRes = if (isUp) R.color.percentage_positive_red else R.color.percentage_negative_blue
        holder.itemView.setBackgroundResource(bgColorRes)

        when (holder) {
            is SellViewHolder -> holder.bind(item)
            is BuyViewHolder -> holder.bind(item)
            is CurrentPriceViewHolder -> holder.bind(item)
            is GapViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newList: List<OrderBookItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

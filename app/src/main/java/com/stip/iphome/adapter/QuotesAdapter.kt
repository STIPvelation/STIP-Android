package com.stip.stip.iphome.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemIpHomeQuoteBinding
import com.stip.stip.iphome.model.PriceChangeStatus
import com.stip.stip.iphome.model.QuoteTick
import java.text.DecimalFormat

class QuotesAdapter(private val context: Context) : ListAdapter<QuoteTick, QuotesAdapter.QuoteViewHolder>(QuoteDiffCallback()) {

    // DecimalFormat 인스턴스 (재사용 가능)
    private val volumeFormatter = DecimalFormat("#,##0.00") // 소수점 두 자리 포맷터
    private val priceFormatter = DecimalFormat("#,##0.00") // 가격 포맷터 (필요 시 수정)


    inner class QuoteViewHolder(private val binding: ItemIpHomeQuoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: QuoteTick) {
            binding.itemTimeTextView.text = quote.time
            // 가격 포맷팅 적용
            binding.itemPriceTextView.text = priceFormatter.format(quote.price)

            // !!! 체결량 포맷팅 변경 !!!
            binding.itemVolumeTextView.text = volumeFormatter.format(quote.volume) // 소수점 두 자리로 포맷팅

            // 가격 변동 상태에 따라 텍스트 색상 변경
            val priceColor = when (quote.priceChangeStatus) {
                PriceChangeStatus.UP -> ContextCompat.getColor(context, R.color.color_rise)
                PriceChangeStatus.DOWN -> ContextCompat.getColor(context, R.color.color_fall)
                PriceChangeStatus.SAME -> ContextCompat.getColor(context, R.color.price_same_color)
            }
            binding.itemPriceTextView.setTextColor(priceColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemIpHomeQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// QuoteDiffCallback 클래스는 변경 없음
class QuoteDiffCallback : DiffUtil.ItemCallback<QuoteTick>() {
    override fun areItemsTheSame(oldItem: QuoteTick, newItem: QuoteTick): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: QuoteTick, newItem: QuoteTick): Boolean {
        return oldItem == newItem
    }
}
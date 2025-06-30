package com.stip.stip.iphome.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemIpHomeQuoteDailyBinding
import com.stip.stip.iphome.model.QuoteTickDaily
import java.text.DecimalFormat
import kotlin.math.abs

class DailyQuotesAdapter(private val context: Context) :
    ListAdapter<QuoteTickDaily, DailyQuotesAdapter.DailyQuoteViewHolder>(DailyQuoteDiffCallback()) {

    private val numberFormat = DecimalFormat("#,##0.00")

    inner class DailyQuoteViewHolder(private val binding: ItemIpHomeQuoteDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuoteTickDaily) {
            binding.itemDateTextView.text = item.date
            binding.itemClosePriceTextView.text = numberFormat.format(item.closePrice)
            binding.itemVolumeTextView.text = numberFormat.format(item.volume)

            val change = item.changeFromPrevious
            val sign = when {
                change > 0 -> "+"
                change < 0 -> "-"
                else -> ""
            }

            // 전일대비 텍스트
            binding.itemChangeTextView.text = "$sign${numberFormat.format(abs(change))}"

            // 색상 처리
            val color = when {
                change > 0 -> ContextCompat.getColor(context, R.color.color_rise)
                change < 0 -> ContextCompat.getColor(context, R.color.color_fall)
                else -> ContextCompat.getColor(context, R.color.price_same_color)
            }

            binding.itemClosePriceTextView.setTextColor(color)
            binding.itemChangeTextView.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyQuoteViewHolder {
        val binding = ItemIpHomeQuoteDailyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyQuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyQuoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DailyQuoteDiffCallback : DiffUtil.ItemCallback<QuoteTickDaily>() {
    override fun areItemsTheSame(oldItem: QuoteTickDaily, newItem: QuoteTickDaily): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: QuoteTickDaily, newItem: QuoteTickDaily): Boolean {
        return oldItem == newItem
    }
}

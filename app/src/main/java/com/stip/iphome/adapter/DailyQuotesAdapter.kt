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
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class DailyQuotesAdapter(private val context: Context) :
    ListAdapter<QuoteTickDaily, DailyQuotesAdapter.DailyQuoteViewHolder>(DailyQuoteDiffCallback()) {

    private val numberFormat = DecimalFormat("#,##0.00")
    private val volumeFormat = DecimalFormat("#,##0.####")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    inner class DailyQuoteViewHolder(private val binding: ItemIpHomeQuoteDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuoteTickDaily) {
            // 날짜 표시
            binding.itemDateTextView.text = item.date

            // 종가 표시
            binding.itemClosePriceTextView.text = numberFormat.format(item.closePrice)

            // 전일 대비 변동률 계산 및 표시
            val changePercent = item.changePercent
            val changeText = when {
                changePercent > 0 -> "+${numberFormat.format(changePercent)}%"
                changePercent < 0 -> "${numberFormat.format(changePercent)}%"
                else -> "0.00%"
            }
            binding.itemChangeTextView.text = changeText

            // 거래량 표시
            binding.itemVolumeTextView.text = volumeFormat.format(item.volume)

            // 가격 변동에 따른 색상 처리
            val color = when {
                changePercent > 0 -> ContextCompat.getColor(context, R.color.color_rise)
                changePercent < 0 -> ContextCompat.getColor(context, R.color.color_fall)
                else -> ContextCompat.getColor(context, R.color.price_same_color)
            }

            binding.itemChangeTextView.setTextColor(color)
            binding.itemClosePriceTextView.setTextColor(color)
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
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: QuoteTickDaily, newItem: QuoteTickDaily): Boolean {
        return oldItem == newItem
    }
}

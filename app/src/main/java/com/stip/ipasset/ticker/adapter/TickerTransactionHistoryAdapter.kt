package com.stip.ipasset.ticker.adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemIpAssetTickerTransactionHistoryBinding
import com.stip.stip.ipasset.model.TransactionHistory
import java.util.Locale

/**
 * 티커 거래내역 리스트 어댑터
 * 티커 트랜잭션만을 위한 전용 어댑터
 */
class TickerTransactionHistoryAdapter(
    private val onItemClick: (TransactionHistory) -> Unit
) : ListAdapter<TransactionHistory, TickerTransactionHistoryAdapter.ViewHolder>(ItemCallback()) {

    private val simpleDateTimeFormat by lazy {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    }

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemIpAssetTickerTransactionHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemCallback : DiffUtil.ItemCallback<TransactionHistory>() {
        override fun areItemsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemIpAssetTickerTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(item: TransactionHistory) {
            binding.formattedAmount.text = "${String.format("%,.2f", item.quantity)} ${item.currencyCode}"
            
            // USD 환산 금액 표시 - 티커 거래 내역에 USD 금액 표시
            if (item.usdAmount > 0) {
                binding.usdAmount.text = "${String.format("%,.2f", item.usdAmount / 100.0)} USD"
                binding.usdAmount.visibility = android.view.View.VISIBLE
            } else {
                binding.usdAmount.text = "-- USD"
                binding.usdAmount.visibility = android.view.View.VISIBLE
            }

            // Define separate variables to avoid destructuring ambiguity
            val statusInfo: Pair<Int, Int> = when (item.status) {
                TransactionHistory.Status.DEPOSIT_COMPLETED -> Pair(R.string.deposit_completed, R.color.text_red_DD3D43_100)
                TransactionHistory.Status.WITHDRAWAL_COMPLETED -> Pair(R.string.withdrawal_completed, R.color.text_blue_0064E6_100)
                TransactionHistory.Status.REFUND_COMPLETED -> Pair(R.string.filter_refund, R.color.text_red_DD3D43_100)
                TransactionHistory.Status.PROCESSING -> Pair(R.string.filter_processing, R.color.gray_600)
            }
            
            val statusResId = statusInfo.first
            val colorRes = statusInfo.second

            binding.status.setText(statusResId)
            binding.status.setTextColor(context.getColor(colorRes))

            binding.date.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(item.timestamp)
            binding.time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.timestamp)

            // 아이템 클릭 리스너
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

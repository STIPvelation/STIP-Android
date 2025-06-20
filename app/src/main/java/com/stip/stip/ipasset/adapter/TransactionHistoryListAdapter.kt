package com.stip.stip.ipasset.adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemTransactionHistoryBinding
import com.stip.stip.ipasset.model.TransactionHistory
import java.util.Locale

class TransactionHistoryListAdapter(
    private val onItemClick: (TransactionHistory) -> Unit // ✨ 추가
) : ListAdapter<TransactionHistory, TransactionHistoryListAdapter.ViewHolder>(ItemCallback()) {

    private val simpleDateTimeFormat by lazy {
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    }

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTransactionHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(item = getItem(position))
    }

    class ItemCallback : DiffUtil.ItemCallback<TransactionHistory>() {
        override fun areItemsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionHistory, newItem: TransactionHistory): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(item: TransactionHistory) {
            binding.quantityText.text = "${String.format("%,.2f", item.quantity)} ${item.currencyCode}"
            binding.formattedAmount.text = ""
            binding.usdAmount.visibility = GONE

            val (statusResId, colorRes) = when (item.status) {
                TransactionHistory.Status.DEPOSIT_COMPLETED -> R.string.deposit_completed to R.color.text_red_DD3D43_100
                TransactionHistory.Status.WITHDRAWAL_COMPLETED -> R.string.withdrawal_completed to R.color.text_blue_0064E6_100
            }

            binding.status.setText(statusResId)
            binding.status.setTextColor(context.getColor(colorRes))

            binding.time.text = simpleDateTimeFormat.format(item.timestamp)

            // ✨ 여기 추가 (아이템 클릭)
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

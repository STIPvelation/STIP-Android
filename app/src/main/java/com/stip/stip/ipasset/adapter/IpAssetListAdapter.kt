package com.stip.stip.ipasset.adapter

import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.databinding.ItemIpAssetBinding
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.R


class IpAssetListAdapter(
    private val onItemClick: (IpAsset) -> Unit
) : ListAdapter<IpAsset, IpAssetListAdapter.ViewHolder>(ItemCallback()) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemIpAssetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position) // position 같이 넘겨줘야 해!
    }


    class ItemCallback : DiffUtil.ItemCallback<IpAsset>() {
        override fun areItemsTheSame(oldItem: IpAsset, newItem: IpAsset): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IpAsset, newItem: IpAsset): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemIpAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IpAsset, position: Int) {
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            if (position == 0) {
                // 1번은 무조건 USD
                binding.name.text = "USD"
                binding.amount.text = String.format("%,.2f", item.amount.toDouble())
                binding.usdAmount.visibility = VISIBLE // USD에서도 하단 금액 표시
                binding.usdAmount.text = String.format("%,.2f USD", item.usdValue.toDouble())
                
                // USD인 경우: 달러 아이콘 보이기
                binding.imageView.setImageResource(R.drawable.ic_dollar_sign)
            } else {
                // 나머지는 티커
                binding.name.text = item.currencyCode
                binding.amount.text = String.format("%,.2f", item.amount.toDouble())
                binding.usdAmount.visibility = VISIBLE // 다른 티커에서는 하단 금액 표시
                binding.usdAmount.text = String.format("%,.2f USD", item.usdValue.toDouble())
                
                // 티커는 빈 이미지 세팅 (또는 나중에 로고 세팅)
                binding.imageView.setImageDrawable(null)
            }
        }
    }
}
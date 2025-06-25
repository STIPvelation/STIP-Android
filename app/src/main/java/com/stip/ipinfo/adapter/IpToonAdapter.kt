package com.stip.stip.ipinfo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.databinding.ItemIpToonBinding
import com.stip.stip.ipinfo.model.IpToonItem

class IpToonAdapter(private val items: List<IpToonItem>) : RecyclerView.Adapter<IpToonAdapter.IpToonViewHolder>() {

    inner class IpToonViewHolder(private val binding: ItemIpToonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IpToonItem) {
            binding.dipMarketLogo1.text = item.title
            binding.logo.setImageResource(item.logoResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IpToonViewHolder {
        val binding = ItemIpToonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IpToonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IpToonViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
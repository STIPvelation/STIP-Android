package com.stip.stip.more.fragment.ipentertainment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stip.stip.R
import com.stip.stip.more.fragment.ipentertainment.model.SwapModel

class SwapAdapter : ListAdapter<SwapModel, SwapAdapter.SwapViewHolder>(SwapDiffCallback()) {

    private var onItemClickListener: ((SwapModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (SwapModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_swap, parent, false)
        return SwapViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwapViewHolder, position: Int) {
        val swap = getItem(position)
        holder.bind(swap)
    }

    inner class SwapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageSwap: ImageView = itemView.findViewById(R.id.imageSwap)
        private val textSwapTitle: TextView = itemView.findViewById(R.id.textSwapTitle)
        private val textSwapDescription: TextView = itemView.findViewById(R.id.textSwapDescription)
        private val textSwapWantsList: TextView = itemView.findViewById(R.id.textSwapWantsList)
        private val badgePopular: TextView = itemView.findViewById(R.id.badgePopular)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(getItem(position))
                }
            }
        }

        fun bind(swap: SwapModel) {
            textSwapTitle.text = swap.title
            textSwapDescription.text = swap.description
            textSwapWantsList.text = swap.wantedItems.joinToString(", ")
            
            // 이미지 로딩
            Glide.with(itemView.context)
                .load(swap.imageUrl)
                .placeholder(R.drawable.ic_ipentertainment_swap)
                .error(R.drawable.ic_ipentertainment_swap)
                .into(imageSwap)
            
            // 인기 아이템 뱃지 표시
            badgePopular.visibility = if (swap.isPopular) View.VISIBLE else View.GONE
        }
    }

    class SwapDiffCallback : DiffUtil.ItemCallback<SwapModel>() {
        override fun areItemsTheSame(oldItem: SwapModel, newItem: SwapModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SwapModel, newItem: SwapModel): Boolean {
            return oldItem == newItem
        }
    }
}

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
import com.stip.stip.more.fragment.ipentertainment.model.AuctionModel
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AuctionAdapter : ListAdapter<AuctionModel, AuctionAdapter.AuctionViewHolder>(AuctionDiffCallback()) {

    private var onItemClickListener: ((AuctionModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (AuctionModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auction, parent, false)
        return AuctionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
        val auction = getItem(position)
        holder.bind(auction)
    }

    inner class AuctionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageAuction: ImageView = itemView.findViewById(R.id.imageAuction)
        private val textAuctionTitle: TextView = itemView.findViewById(R.id.textAuctionTitle)
        private val textAuctionPrice: TextView = itemView.findViewById(R.id.textAuctionPrice)
        private val textTimeRemaining: TextView = itemView.findViewById(R.id.textTimeRemaining)
        private val badgeFeatured: TextView = itemView.findViewById(R.id.badgeFeatured)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(getItem(position))
                }
            }
        }

        fun bind(auction: AuctionModel) {
            textAuctionTitle.text = auction.title
            
            // 가격 포맷팅
            val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA)
                .format(auction.currentPrice)
            textAuctionPrice.text = "₩ $formattedPrice"
            
            // 남은 시간 계산
            val remainingTime = getRemainingTimeText(auction.endTime)
            textTimeRemaining.text = "남은 시간: $remainingTime"
            
            // 이미지 로딩
            Glide.with(itemView.context)
                .load(auction.imageUrl)
                .placeholder(R.drawable.ic_ipentertainment_auction)
                .error(R.drawable.ic_ipentertainment_auction)
                .into(imageAuction)
            
            // 특별 뱃지 표시
            badgeFeatured.visibility = if (auction.isFeatured) View.VISIBLE else View.GONE
        }
        
        private fun getRemainingTimeText(endTime: Date): String {
            val now = Date()
            val diffInMillis = endTime.time - now.time
            
            if (diffInMillis <= 0) {
                return "마감됨"
            }
            
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60
            
            return when {
                days > 0 -> "${days}일 ${hours}시간"
                hours > 0 -> "${hours}시간 ${minutes}분"
                else -> "${minutes}분"
            }
        }
    }

    class AuctionDiffCallback : DiffUtil.ItemCallback<AuctionModel>() {
        override fun areItemsTheSame(oldItem: AuctionModel, newItem: AuctionModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AuctionModel, newItem: AuctionModel): Boolean {
            return oldItem == newItem
        }
    }
}

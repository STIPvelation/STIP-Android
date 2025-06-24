package com.stip.stip.more.fragment.ipentertainment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stip.stip.R
import com.stip.stip.more.fragment.ipentertainment.model.DonationModel
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DonationAdapter : ListAdapter<DonationModel, DonationAdapter.DonationViewHolder>(DonationDiffCallback()) {

    private var onItemClickListener: ((DonationModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (DonationModel) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = getItem(position)
        holder.bind(donation)
    }

    inner class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageDonation: ImageView = itemView.findViewById(R.id.imageDonation)
        private val textDonationTitle: TextView = itemView.findViewById(R.id.textDonationTitle)
        private val textDonationOrganizer: TextView = itemView.findViewById(R.id.textDonationOrganizer)
        private val progressDonation: ProgressBar = itemView.findViewById(R.id.progressDonation)
        private val textDonationAmount: TextView = itemView.findViewById(R.id.textDonationAmount)
        private val textDonationGoal: TextView = itemView.findViewById(R.id.textDonationGoal)
        private val textDaysLeft: TextView = itemView.findViewById(R.id.textDaysLeft)
        private val badgeUrgent: TextView = itemView.findViewById(R.id.badgeUrgent)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(getItem(position))
                }
            }
        }

        fun bind(donation: DonationModel) {
            textDonationTitle.text = donation.title
            textDonationOrganizer.text = "주최: ${donation.organizer}"
            
            // 진행률 설정
            progressDonation.progress = donation.getProgress()
            
            // 금액 포맷팅
            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
            textDonationAmount.text = "₩ ${numberFormat.format(donation.currentAmount)}"
            textDonationGoal.text = "목표: ₩ ${numberFormat.format(donation.goalAmount)}"
            
            // 남은 기간 계산
            val daysLeft = getDaysLeft(donation.endDate)
            textDaysLeft.text = "남은 기간: ${daysLeft}일"
            
            // 이미지 로딩
            Glide.with(itemView.context)
                .load(donation.imageUrl)
                .placeholder(R.drawable.ic_ipentertainment_donation)
                .error(R.drawable.ic_ipentertainment_donation)
                .into(imageDonation)
            
            // 긴급 뱃지
            badgeUrgent.visibility = if (donation.isUrgent) View.VISIBLE else View.GONE
        }
        
        private fun getDaysLeft(endDate: Date): Long {
            val now = Date()
            val diffInMillis = endDate.time - now.time
            return if (diffInMillis <= 0) 0 else TimeUnit.MILLISECONDS.toDays(diffInMillis)
        }
    }

    class DonationDiffCallback : DiffUtil.ItemCallback<DonationModel>() {
        override fun areItemsTheSame(oldItem: DonationModel, newItem: DonationModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DonationModel, newItem: DonationModel): Boolean {
            return oldItem == newItem
        }
    }
}

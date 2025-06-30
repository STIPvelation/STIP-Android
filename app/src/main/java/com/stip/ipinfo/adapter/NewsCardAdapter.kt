package com.stip.stip.ipinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.ipinfo.model.NewsItem

class NewsCardAdapter(
    private val newsList: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsCardAdapter.NewsViewHolder>() {

    // Sample image resources to use for news items
    private val sampleImages = arrayOf(
        R.drawable.sample_news_1,
        R.drawable.sample_news_2,
        R.drawable.sample_news_3,
        R.drawable.news_placeholder
    )

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.findViewById(R.id.news_title)
        val newsDate: TextView = itemView.findViewById(R.id.news_date)
        val newsImage: ImageView = itemView.findViewById(R.id.news_image)

        fun bind(newsItem: NewsItem) {
            newsTitle.text = newsItem.title
            newsDate.text = newsItem.pubDate
            
            // Assign a random sample image based on the position (for demonstration)
            val imageResource = sampleImages[newsItem.title.hashCode().rem(sampleImages.size).let { if (it < 0) it + sampleImages.size else it }]
            newsImage.setImageResource(imageResource)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(newsItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_news_item_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount() = newsList.size
}

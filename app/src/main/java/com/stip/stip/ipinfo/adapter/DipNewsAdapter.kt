package com.stip.stip.ipinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.ipinfo.model.DipNewsItem

class DipNewsAdapter(
    private val items: List<DipNewsItem>
) : RecyclerView.Adapter<DipNewsAdapter.DipNewsViewHolder>() {

    inner class DipNewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.dip_listing_info_logo_10)
        val description: TextView = view.findViewById(R.id.dipdescription)
        val date: TextView = view.findViewById(R.id.dip_reprot_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DipNewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_dip_listing_item, parent, false)
        return DipNewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: DipNewsViewHolder, position: Int) {
        val item = items[position]

        if (item.logoResId != null) {
            holder.logo.setImageResource(item.logoResId)
            holder.logo.visibility = View.VISIBLE
        } else {
            holder.logo.setImageDrawable(null)
            holder.logo.visibility = View.INVISIBLE // 공간 유지
        }

        holder.description.text = item.description
        holder.date.text = item.date
    }

    override fun getItemCount(): Int = items.size
}
package com.stip.stip.ipinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.ipinfo.model.DipMarketCapItem

class DipMarketCapAdapter(
    private val items: List<DipMarketCapItem>
) : RecyclerView.Adapter<DipMarketCapAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.logo)
        val name: TextView = view.findViewById(R.id.dip_market_logo_1)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ip_toon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 로고 처리 (null-safe)
        if (item.logoResId != null) {
            holder.logo.setImageResource(item.logoResId)
            holder.logo.visibility = View.VISIBLE
        } else {
            holder.logo.setImageDrawable(null)
            holder.logo.visibility = View.INVISIBLE
        }

        holder.name.text = item.name

    }

    override fun getItemCount(): Int = items.size
}
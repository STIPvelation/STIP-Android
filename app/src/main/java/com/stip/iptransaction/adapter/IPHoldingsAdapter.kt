package com.stip.iptransaction.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stip.dummy.IPHoldingDummyData
import com.stip.stip.R

/**
 * Adapter for displaying IP Holdings in a RecyclerView
 */
class IPHoldingsAdapter(private val context: Context) :
    RecyclerView.Adapter<IPHoldingsAdapter.ViewHolder>() {

    private var holdingsList: List<IPHoldingDummyData.IPHoldingItem> = emptyList()

    fun setData(items: List<IPHoldingDummyData.IPHoldingItem>) {
        holdingsList = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = holdingsList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = holdingsList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.item_1_text_asset_name)
        private val returnRateTextView: TextView = itemView.findViewById(R.id.item_1_text_return_rate_value)
        private val quantityTextView: TextView = itemView.findViewById(R.id.item_1_text_quantity_value)
        private val avgPriceTextView: TextView = itemView.findViewById(R.id.item_1_text_avg_price_value)
        private val valuationTextView: TextView = itemView.findViewById(R.id.item_1_text_valuation_value)
        private val purchaseAmountTextView: TextView = itemView.findViewById(R.id.item_1_text_purchase_amount_value)
        private val profitLossTextView: TextView = itemView.findViewById(R.id.item_1_text_pl_value)

        fun bind(item: IPHoldingDummyData.IPHoldingItem) {
            // Set asset name
            nameTextView.text = item.name

            // Set quantity
            quantityTextView.text = item.holdingQuantity.toString()

            // Set average buy price
            avgPriceTextView.text = item.buyPrice

            // Set valuation price
            valuationTextView.text = item.currentPrice

            // Set purchase amount
            purchaseAmountTextView.text = item.valueInUsd

            // Set profit/loss
            profitLossTextView.text = item.profitLoss
            val profitLossColor = if (item.profitLossPercentage >= 0)
                Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            profitLossTextView.setTextColor(profitLossColor)

            // Set return rate
            val returnRateText = if (item.profitLossPercentage >= 0)
                "+${item.profitLossPercentage}%" else "${item.profitLossPercentage}%"
            returnRateTextView.text = returnRateText
            returnRateTextView.setTextColor(profitLossColor)
        }
    }
}
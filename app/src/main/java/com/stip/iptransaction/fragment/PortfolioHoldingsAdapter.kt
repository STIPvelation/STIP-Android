package com.stip.stip.iptransaction.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.iptransaction.model.PortfolioIPHoldingDto
import java.math.BigDecimal

class PortfolioHoldingsAdapter(private var holdings: List<PortfolioIPHoldingDto>) :
    RecyclerView.Adapter<PortfolioHoldingsAdapter.PortfolioViewHolder>() {

    inner class PortfolioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val assetName: TextView = itemView.findViewById(R.id.item_1_text_asset_name)
        private val quantityValue: TextView = itemView.findViewById(R.id.item_1_text_quantity_value)
        private val avgPriceValue: TextView = itemView.findViewById(R.id.item_1_text_avg_price_value)
        private val valuationValue: TextView = itemView.findViewById(R.id.item_1_text_valuation_value)
        private val purchaseAmountValue: TextView = itemView.findViewById(R.id.item_1_text_purchase_amount_value)
        private val plValue: TextView = itemView.findViewById(R.id.item_1_text_pl_value)
        private val returnRateValue: TextView = itemView.findViewById(R.id.item_1_text_return_rate_value)

        fun bind(holding: PortfolioIPHoldingDto) {
            assetName.text = holding.name

            quantityValue.text = "${holding.balance}"
            avgPriceValue.text = "$%,.2f".format(holding.buyAvgPrice.toDouble())
            valuationValue.text = "$%,.2f".format(holding.evalAmount.toDouble())
            purchaseAmountValue.text = "$%,.2f".format(holding.buyAmount.toDouble())

            val profitFormatted = if (holding.profit >= BigDecimal.ZERO) "+$%,.2f".format(holding.profit.toDouble()) else "-$%,.2f".format(holding.profit.abs().toDouble())
            val profitRateFormatted = "%.2f%%".format(holding.profitRate.toDouble())
            val colorRes = if (holding.profit >= BigDecimal.ZERO) R.color.color_rise else R.color.color_fall
            val color = ContextCompat.getColor(itemView.context, colorRes)

            plValue.apply {
                text = profitFormatted
                setTextColor(color)
            }

            returnRateValue.apply {
                text = profitRateFormatted
                setTextColor(color)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_dip, parent, false)
        return PortfolioViewHolder(view)
    }

    override fun onBindViewHolder(holder: PortfolioViewHolder, position: Int) {
        val holding = holdings[position]
        holder.bind(holding)
    }

    override fun getItemCount(): Int {
        return holdings.size
    }

    fun updateData(newHoldings: List<PortfolioIPHoldingDto>) {
        this.holdings = newHoldings
        notifyDataSetChanged()
    }
} 
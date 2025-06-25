package com.stip.stip.iphome.adapter  // 너 프로젝트 구조에 맞게 패키지명 수정해도 돼

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.iptransaction.model.IpInvestmentItem

class FilledOrderAdapter : RecyclerView.Adapter<FilledOrderAdapter.FilledOrderViewHolder>() {

    private val items = mutableListOf<IpInvestmentItem>()

    fun submitList(data: List<IpInvestmentItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    inner class FilledOrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameType = view.findViewById<TextView>(R.id.text_name_type)
        private val time = view.findViewById<TextView>(R.id.text_execution_time)
        private val unitPrice = view.findViewById<TextView>(R.id.text_unit_price)
        private val quantity = view.findViewById<TextView>(R.id.text_quantity)
        private val amount = view.findViewById<TextView>(R.id.text_amount)

        fun bind(item: IpInvestmentItem) {
            nameType.text = "${item.name} ${item.type}"
            time.text = item.executionTime
            unitPrice.text = item.unitPrice
            quantity.text = item.quantity
            amount.text = item.amount
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilledOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filled_order, parent, false)
        return FilledOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilledOrderViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

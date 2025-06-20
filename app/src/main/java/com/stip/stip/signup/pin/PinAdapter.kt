package com.stip.stip.signup.pin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.signup.base.BaseViewHolder
import com.stip.stip.databinding.ItemRvPinBinding
import com.stip.stip.signup.utils.GlideUtils

class PinAdapter(
    private var itemList: MutableList<Boolean>,
): RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemRvPinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PinViewHolder(private var binding: ItemRvPinBinding): BaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            val itemData = itemList[position]

            if (itemData) {
                GlideUtils.loadImage(binding.ivPinDot, R.drawable.ic_dot_sky_11dp)
            } else {
                GlideUtils.loadImage(binding.ivPinDot, R.drawable.ic_dot_gray_11dp)
            }
        }
    }

    fun updatePinCount(count: Int) {
        itemList = List(itemList.size) { it < count }.toMutableList()
        notifyDataSetChanged()
    }
}
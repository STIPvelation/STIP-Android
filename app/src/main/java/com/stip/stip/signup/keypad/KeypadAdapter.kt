package com.stip.stip.signup.keypad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.signup.base.BaseViewHolder
import com.stip.stip.databinding.ItemRvKeypadBinding

class KeypadAdapter(
    private var itemList: MutableList<KeypadItem>,
    private val onItemClick: (KeypadItem) -> Unit
): RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemRvKeypadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeyPadViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class KeyPadViewHolder(private var binding: ItemRvKeypadBinding): BaseViewHolder(binding.root) {
        override fun bind(position: Int) {
            val itemData = itemList[position]

            if (itemData.iconRes == null) {
                binding.clKeypadDelContainer.visibility = View.GONE
                binding.clKeypadNumberContainer.visibility = View.VISIBLE

                when(itemData.type) {
                    KeypadType.NUMBER -> {
                        binding.tvKeypadNumber.text = itemData.value
                        binding.tvKeypadNumber.setTextColor(binding.root.context.getColor(android.R.color.black))
                    }
                    KeypadType.SHUFFLE -> {
                        binding.tvKeypadNumber.text = itemData.value // "↻" 아이콘 사용
                        binding.tvKeypadNumber.setTextColor(binding.root.context.getColor(android.R.color.black))
                    }
                    else -> {}
                }
            } else {
                binding.clKeypadDelContainer.visibility = View.VISIBLE
                binding.clKeypadNumberContainer.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick.invoke(itemData)
            }
        }
    }

    fun shuffleNumbers() {
        val numberItems = itemList.filter { it.type == KeypadType.NUMBER }.shuffled() // 0~9 숫자 모두 섞기
        val fixedItems = listOf(
            KeypadItem("↻", KeypadType.SHUFFLE), // 아이콘 사용
            numberItems.first(), // 섞인 숫자 중 첫 번째 값을 "0" 자리에 고정
            KeypadItem("", KeypadType.DELETE, R.drawable.ic_del_white_31dp)
        )

        itemList = (numberItems.drop(1) + fixedItems).toMutableList() // 첫 번째 숫자를 제외하고 나머지를 추가
        notifyDataSetChanged()
    }
}
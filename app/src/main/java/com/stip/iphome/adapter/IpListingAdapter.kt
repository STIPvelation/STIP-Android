package com.stip.stip.iphome.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemIpListingBinding
import com.stip.stip.iphome.fragment.TradingFragment
import com.stip.stip.iphome.model.IpListingItem
import com.stip.stip.iphome.model.IpCategory

class IpListingAdapter(var items: List<IpListingItem>) :
    RecyclerView.Adapter<IpListingAdapter.IpListingViewHolder>() {

    inner class IpListingViewHolder(val binding: ItemIpListingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IpListingItem) {
            val context = binding.root.context

            // 기본 배경색
            binding.frame7484.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

            // 거래 발생 시 깜빡임 효과
            if (item.isTradeTriggered) {
                val flashColor = if (item.isBuy) {
                    ContextCompat.getColor(context, R.color.trade_buy_background)
                } else {
                    ContextCompat.getColor(context, R.color.trade_sell_background)
                }
                binding.frame7484.setBackgroundColor(flashColor)
                binding.frame7484.postDelayed({
                    binding.frame7484.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                }, 300)
            }

            // 텍스트 설정
            binding.itemTickerName.text = item.ticker
            binding.itemCurrentPrice.text = item.currentPrice
            binding.itemChangePercent.text = item.changePercent
            binding.itemChangeAbsolute.text = item.changeAbsolute
            binding.itemVolume.text = item.volume

            // 상승/하락에 따른 텍스트 색상
            val priceColor = when {
                item.changePercent.startsWith("+") -> ContextCompat.getColor(context, R.color.red)
                item.changePercent.startsWith("-") -> ContextCompat.getColor(context, R.color.blue)
                else -> ContextCompat.getColor(context, R.color.text_primary)
            }

            binding.itemCurrentPrice.setTextColor(priceColor)
            binding.itemChangePercent.setTextColor(priceColor)
            binding.itemChangeAbsolute.setTextColor(priceColor)
            binding.itemVolume.setTextColor(ContextCompat.getColor(context, R.color.text_primary))

            // OHLC 데이터 세팅
            val open = item.open.toFloatOrNull() ?: 0f
            val high = item.high.toFloatOrNull() ?: 0f
            val low = item.low.toFloatOrNull() ?: 0f
            val close = item.close.toFloatOrNull() ?: 0f
            val isRise = close >= open

            binding.viewOhlc.setOhlc(open, high, low, close, isRise)

            // 카테고리 텍스트 설정 (enum 적용)
            val category = IpCategory.fromRaw(item.category)
            binding.itemCategoryText.text = category.toDisplayString(context)

            // 텍스트 깜빡임
            if (item.isTradeTriggered) {
                animatePriceFlash(binding.itemCurrentPrice, item.isBuy)
            }

            // 클릭 시 거래 화면으로 이동
            binding.root.setOnClickListener {
                val fragment = TradingFragment.newInstance(item.ticker, item.companyName)
                val activity = binding.root.context as? AppCompatActivity
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }

            // 로그 출력
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                Log.d(
                    "IpListingAdapter",
                    "[$position] isTradeTriggered=${item.isTradeTriggered}, isBuy=${item.isBuy}"
                )
            }
        }

        private fun animatePriceFlash(view: TextView, isBuy: Boolean) {
            val borderDrawable =
                if (isBuy) R.drawable.flash_red_border else R.drawable.flash_blue_border
            view.setBackgroundResource(borderDrawable)
            view.postDelayed({ view.setBackgroundResource(0) }, 300)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IpListingViewHolder {
        val binding =
            ItemIpListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IpListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IpListingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<IpListingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

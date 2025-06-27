package com.stip.stip.ipasset.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.databinding.ItemIpAssetBinding
import com.stip.stip.ipasset.model.IpAsset
import com.stip.stip.R


class IpAssetListAdapter(
    private val onItemClick: (IpAsset) -> Unit
) : ListAdapter<IpAsset, IpAssetListAdapter.ViewHolder>(ItemCallback()) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemIpAssetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position) // position 같이 넘겨줘야 해!
    }


    class ItemCallback : DiffUtil.ItemCallback<IpAsset>() {
        override fun areItemsTheSame(oldItem: IpAsset, newItem: IpAsset): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IpAsset, newItem: IpAsset): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemIpAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IpAsset, position: Int) {
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            val currencyCode = item.currencyCode
            val context = binding.root.context
            
            // 티커 이름 설정
            binding.name.text = currencyCode
            
            // 금액 설정 - 티커 코드 없이 금액만 표시
            binding.amount.text = String.format("%,d", item.amount)
            
            // USD 가치 설정
            if (currencyCode == "USD") {
                // USD 항목은 하단 금액 표시 안함
                binding.usdAmount.visibility = View.GONE
            } else if (item.usdValue > 0) {
                // 티커 항목은 $ 기호 추가 (공백 없이 붙여서 표시)
                binding.usdAmount.visibility = View.VISIBLE
                binding.usdAmount.text = "$" + String.format("%,.2f", item.usdValue.toDouble())
            } else {
                binding.usdAmount.visibility = View.VISIBLE
                binding.usdAmount.text = "$0.00"
            }
            
            // 로고 배경 색상 설정
            val backgroundColorRes = when(currencyCode) {
                "USD" -> R.color.token_usd
                "JWV" -> R.color.token_jwv
                "MDM" -> R.color.token_mdm
                "CDM" -> R.color.token_cdm
                "IJECT" -> R.color.token_iject
                "WETALK" -> R.color.token_wetalk
                "SLEEP" -> R.color.token_sleep
                "KCOT" -> R.color.token_kcot
                "MSK" -> R.color.token_msk
                "SMT" -> R.color.token_smt
                "AXNO" -> R.color.token_axno
                "KATV" -> R.color.token_katv
                else -> R.color.token_usd // 기본값
            }
            
            // 로고 원형 배경 색상 적용
            val logoBackground = binding.root.findViewById<View>(R.id.token_logo_background)
            logoBackground.setBackgroundResource(R.drawable.bg_circle_token)
            logoBackground.backgroundTintList = context.getColorStateList(backgroundColorRes)
            
            // 로고 이미지나 텍스트 설정
            val logoText = binding.root.findViewById<TextView>(R.id.token_logo_text)
            
            // 티커용 이미지가 있는지 확인하고 이미지가 있으면 이미지로 표시
            val resourceId = getTickerLogoResourceId(context, currencyCode)
            
            if (resourceId > 0) {
                // 이미지 리소스가 있는 경우
                logoText.visibility = View.GONE
                
                // 이미지뷰 찾기
                val imageView = binding.root.findViewById<android.widget.ImageView>(R.id.token_logo_image)
                
                imageView.visibility = View.VISIBLE
                imageView.setImageResource(resourceId)
            } else {
                // 이미지 리소스가 없는 경우 텍스트로 표시
                if (currencyCode == "USD") {
                    logoText.text = "$" // USD는 $ 기호 표시
                } else {
                    // 처음 2자만 표시
                    logoText.text = currencyCode.take(2)
                }
            }
        }
        
        // 티커 코드에 해당하는 로고 리소스 ID 가져오기
        private fun getTickerLogoResourceId(context: android.content.Context, tickerCode: String): Int {
            val resName = "ic_ticker_${tickerCode.lowercase()}"
            return context.resources.getIdentifier(resName, "drawable", context.packageName)
        }
    }
}
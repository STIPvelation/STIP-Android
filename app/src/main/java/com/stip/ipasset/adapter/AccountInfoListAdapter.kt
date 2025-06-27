package com.stip.stip.ipasset.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.ItemAccountInfoBinding
import com.stip.stip.ipasset.model.AccountInfoItem

class AccountInfoListAdapter : ListAdapter<AccountInfoItem, AccountInfoListAdapter.ViewHolder>(ItemCallback()) {
    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            viewBinding = ItemAccountInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(item = getItem(position))
    }

    class ItemCallback() : DiffUtil.ItemCallback<AccountInfoItem>() {
        override fun areItemsTheSame(oldItem: AccountInfoItem, newItem: AccountInfoItem): Boolean {
            return oldItem.accountNumber == newItem.accountNumber
        }

        override fun areContentsTheSame(oldItem: AccountInfoItem, newItem: AccountInfoItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(val viewBinding: ItemAccountInfoBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: AccountInfoItem) {
            viewBinding.accountHolder.text = item.accountHolder
            val accountInfo = "${item.bank} ${item.accountNumber}"
            viewBinding.accountNumber.text = accountInfo
            
            // 계좌번호 복사 기능 추가
            viewBinding.accountContainer.setOnClickListener {
                copyToClipboard(it.context, accountInfo)
            }
            
            // 계좌번호 텍스트뷰 클릭 시에도 복사 기능 추가
            viewBinding.accountNumber.setOnClickListener {
                copyToClipboard(it.context, accountInfo)
            }
        }
        
        private fun copyToClipboard(context: Context, text: String) {
            val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
            val clipData = ClipData.newPlainText("account_number", text)
            clipboardManager?.setPrimaryClip(clipData)
            
            // 안드로이드 13(API 33) 이하에서는 토스트 메시지 표시 (13 이상에서는 시스템이 자동으로 알림 표시)
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
                Toast.makeText(context, context.getString(R.string.account_number_copied), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
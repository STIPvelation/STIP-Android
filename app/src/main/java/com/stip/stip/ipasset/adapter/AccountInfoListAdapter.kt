package com.stip.stip.ipasset.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
            viewBinding.accountNumber.text = "${item.bank} ${item.accountNumber}"
        }
    }
}
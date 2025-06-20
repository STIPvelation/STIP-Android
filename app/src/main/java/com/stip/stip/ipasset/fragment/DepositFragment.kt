package com.stip.stip.ipasset.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentDepositBinding
import com.stip.stip.databinding.LayoutDepositNotUsdBinding
import com.stip.stip.databinding.LayoutDepositUsdBinding
import com.stip.stip.ipasset.DepositViewModel
import com.stip.stip.ipasset.adapter.AccountInfoDecorator
import com.stip.stip.ipasset.adapter.AccountInfoListAdapter
import com.stip.stip.ipasset.extension.copyToClipboard
import com.stip.stip.ipasset.extension.dpToPx
import com.stip.stip.ipasset.model.IpAsset
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DepositFragment : BaseFragment<FragmentDepositBinding>() {
    private val currencyCode: String get() = ipAsset.currencyCode
    private val ipAsset: IpAsset get() = navArgs.ipAsset
    private val navArgs: DepositFragmentArgs by navArgs()

    private val viewModel by viewModels<DepositViewModel>()
    private val adapter = AccountInfoListAdapter()

    private fun bind() = with(viewBinding) {
        materialToolbar.title = "$currencyCode ${getString(R.string.common_deposit_action)}"
        materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 모든 화폐(USD, KRW 등)에 대해 동일한 계좌 정보를 표시합니다
        layoutUsd.root.visibility = View.VISIBLE
        layoutNotUsd.root.visibility = View.GONE

        bindUsdLayout(layoutUsd)
    }

    private fun bindUsdLayout(binding: LayoutDepositUsdBinding) = with(binding) {
        val space = requireContext().dpToPx(4.0F)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(AccountInfoDecorator(space = space.toInt()))
    }

    private fun bindNotUseLayout(binding: LayoutDepositNotUsdBinding) {
        val tag = "{currencyCode}"

        binding.description.text = getString(R.string.deposit_notice_message_not_usd).replace(
            tag,
            ipAsset.currencyCode
        )

        binding.copy.setOnClickListener {
            viewModel.depositUrl.value?.let {
                requireContext().copyToClipboard(
                    label = "Deposit Address",
                    text = it
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind()

        viewModel.fetchAccountInfo(currencyCode)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.depositAccountInfoList.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentDepositBinding {
        return FragmentDepositBinding.inflate(inflater, container, false)
    }
}

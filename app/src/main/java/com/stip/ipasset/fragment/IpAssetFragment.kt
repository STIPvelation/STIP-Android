package com.stip.stip.ipasset.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetBinding
import com.stip.stip.ipasset.DepositKrwActivity
import com.stip.stip.ipasset.IpAssetViewModel
import com.stip.stip.ipasset.TransactionActivity
import com.stip.stip.ipasset.adapter.IpAssetListAdapter
import com.stip.stip.ipasset.model.IpAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class IpAssetFragment : BaseFragment<FragmentIpAssetBinding>() {

    private var isDropdownMenuShowing = AtomicBoolean(false)
    private var currentAssetList: List<IpAsset> = emptyList()

    private val adapter = IpAssetListAdapter {
        if (!isDropdownMenuShowing.get()) {
            startActivity(
                Intent(requireContext(), TransactionActivity::class.java).putExtra(IpAsset.NAME, it)
            )
        }
    }

    private val viewModel by viewModels<IpAssetViewModel>()
    private val filterOptions by lazy {
        listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_owned)
        )
    }


    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetBinding {
        return FragmentIpAssetBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupRecyclerView()
        setupDropdownMenu()
        setupSearchBar()
        collectData()

        viewBinding.buttonKrwDeposit.setOnClickListener {
            val intent = Intent(requireContext(), DepositKrwActivity::class.java)
            startActivity(intent)
        }

        PhoneFraudAlertDialogFragment.newInstance().show(childFragmentManager, null)
    }

    private fun setupRecyclerView() = with(viewBinding.ipAssets) {
        adapter = this@IpAssetFragment.adapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshIpAssets() // 이름 수정했어!
            
            // 2초 타임아웃 추가 - 2초 후에 업데이트가 안되면 자동으로 리프레시 표시 중지
            lifecycleScope.launch {
                delay(2000)
                if (viewBinding.swipeRefreshLayout.isRefreshing) {
                    viewBinding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }


    private fun collectData() {
        lifecycleScope.launch {
            viewModel.totalIpAssets.collect { totalAmount ->
                viewBinding.totalIpAssets.text = String.format(Locale.US, "%,.2f USD", totalAmount.toDouble())
            }
        }

        lifecycleScope.launch {
            viewModel.ipAssets.collect { ipAssets ->
                currentAssetList = ipAssets
                adapter.submitList(buildListWithUsdFirst(currentAssetList))
                viewBinding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupSearchBar() {
        viewBinding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                val filteredList = if (query.isEmpty()) {
                    buildListWithUsdFirst(currentAssetList)
                } else {
                    buildListWithUsdFirst(
                        currentAssetList.filter {
                            it.currencyCode.contains(query, ignoreCase = true)
                        }
                    )
                }
                adapter.submitList(filteredList)
            }
        })
    }

    private fun setupDropdownMenu() = with(viewBinding.dropdownMenu) {
        setDropDownBackgroundDrawable(null)
        setText(filterOptions[0], false)

        setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.layout_filter_option,
                filterOptions
            )
        )

        setOnItemClickListener { _, _, position, _ ->
            val allAssets = this@IpAssetFragment.viewModel.ipAssets.value
            when (filterOptions[position]) {
                getString(R.string.filter_all) -> {
                    this@IpAssetFragment.adapter.submitList(buildListWithUsdFirst(allAssets))
                }
                getString(R.string.filter_owned) -> {
                    this@IpAssetFragment.adapter.submitList(buildListWithUsdFirst(allAssets.filter { it.amount > 0 }))
                }
            }
        }

        setOnDismissListener {
            lifecycleScope.launch {
                delay(300)
                isDropdownMenuShowing.set(false)
            }
        }

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                post { toggleDropdownMenu() }
            }
        }

        setOnClickListener {
            toggleDropdownMenu()
        }
    }


    private fun toggleDropdownMenu() {
        val dropdownMenu = viewBinding.dropdownMenu
        when {
            isDropdownMenuShowing.compareAndSet(false, true) -> dropdownMenu.showDropDown()
            isDropdownMenuShowing.compareAndSet(true, false) -> dropdownMenu.dismissDropDown()
        }
    }

    private fun buildListWithUsdFirst(ipAssets: List<IpAsset>): List<IpAsset> {
        val usdItem = ipAssets.find { it.currencyCode == "USD" }
        val otherItems = ipAssets.filter { it.currencyCode != "USD" }

        return buildList {
            usdItem?.let { add(it) }
            addAll(otherItems)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = IpAssetFragment()
    }
}
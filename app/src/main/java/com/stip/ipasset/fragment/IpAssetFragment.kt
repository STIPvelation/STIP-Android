package com.stip.stip.ipasset.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
            // 실제 API 호출 대신 하드코딩된 데이터를 표시
            lifecycleScope.launch {
                // 0.5초 지연 효과 추가 (사용자 경험 개선)
                delay(500)
                
                // 하드코딩된 데이터를 다시 설정
                val hardcodedAssets = createHardcodedAssets()
                currentAssetList = hardcodedAssets
                adapter.submitList(buildListWithUsdFirst(hardcodedAssets))
                
                // 리프레시 애니메이션 종료
                viewBinding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }


    private fun collectData() {
        // 하드코딩된 총 보유자산 금액 설정
        val totalAmount = 12850.75
        viewBinding.totalIpAssets.text = String.format(Locale.US, "%,.2f USD", totalAmount)
        
        // 하드코딩된 1개의 USD와 11개의 티커 데이터 생성
        val hardcodedAssets = createHardcodedAssets()
        currentAssetList = hardcodedAssets
        adapter.submitList(buildListWithUsdFirst(hardcodedAssets))
        viewBinding.swipeRefreshLayout.isRefreshing = false
        
        // ViewModel 관련 변수 감지
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (!isLoading) {
                    // ViewModel에서 데이터를 가져오는 대신 하드코딩된 데이터를 사용
                    adapter.submitList(buildListWithUsdFirst(hardcodedAssets))
                    viewBinding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
    
    /**
     * 하드코딩된 1개의 USD와 11개의 티커 데이터를 생성하는 함수
     */
    private fun createHardcodedAssets(): List<IpAsset> {
        return listOf(
            // USD 데이터
            IpAsset(1, "USD", 10000, 10000),
            
            // 11개 티커 데이터 (iOS TransactionView.swift 기반)
            IpAsset(2, "JWV", 350, 420),
            IpAsset(3, "MDM", 200, 280),
            IpAsset(4, "CDM", 0, 0),   // 0개 소유 예시
            IpAsset(5, "IJECT", 500, 650),
            IpAsset(6, "WETALK", 120, 130),
            IpAsset(7, "SLEEP", 0, 0),  // 0개 소유 예시
            IpAsset(8, "KCOT", 800, 1100),
            IpAsset(9, "MSK", 50, 70),
            IpAsset(10, "SMT", 0, 0),   // 0개 소유 예시
            IpAsset(11, "AXNO", 150, 180),
            IpAsset(12, "KATV", 20, 20)
        )
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

    private fun setupDropdownMenu() {
        val filterAll = viewBinding.filterAll
        val filterHeld = viewBinding.filterHeld
        
        // 초기 상태 설정 - "전체" 선택
        updateFilterSelection(true, false)
        
        // 전체 필터 클릭 리스너
        filterAll.setOnClickListener {
            updateFilterSelection(true, false)
            // 전체 필터링: 모든 하드코딩 데이터 표시
            this@IpAssetFragment.adapter.submitList(buildListWithUsdFirst(currentAssetList))
        }
        
        // 보유중 필터 클릭 리스너 
        filterHeld.setOnClickListener {
            updateFilterSelection(false, true)
            // 보유중 필터링: 금액이 0보다 큰 자산만 표시
            val heldAssets = currentAssetList.filter { it.amount > 0 }
            this@IpAssetFragment.adapter.submitList(buildListWithUsdFirst(heldAssets))
        }
    }
    
    private fun updateFilterSelection(allSelected: Boolean, heldSelected: Boolean) {
        val filterAll = viewBinding.filterAll
        val filterHeld = viewBinding.filterHeld
        
        // 선택된 필터 스타일 적용
        if (allSelected) {
            filterAll.setBackgroundResource(R.color.sky_30C6E8_10)
            filterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.sky_30C6E8_100))
            filterAll.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold_font_family)
            
            filterHeld.background = null
            filterHeld.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            filterHeld.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular_font_family)
        } else if (heldSelected) {
            filterHeld.setBackgroundResource(R.color.sky_30C6E8_10)
            filterHeld.setTextColor(ContextCompat.getColor(requireContext(), R.color.sky_30C6E8_100))
            filterHeld.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold_font_family)
            
            filterAll.background = null
            filterAll.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            filterAll.typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular_font_family)
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
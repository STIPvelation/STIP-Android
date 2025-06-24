package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreIpAuctionBinding
import com.stip.stip.more.fragment.ipentertainment.adapter.AuctionAdapter
import com.stip.stip.more.fragment.ipentertainment.model.AuctionModel
import java.util.*

class IPAuctionFragment : Fragment() {
    private var _binding: FragmentMoreIpAuctionBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private val TAG = "IPAuctionFragment_Debug"

    private lateinit var auctionAdapter: AuctionAdapter
    private var auctionList = mutableListOf<AuctionModel>()
    private var filteredAuctionList = mutableListOf<AuctionModel>()
    
    private var selectedCategory: String = "전체"
    private var activeTab: Int = 0
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreIpAuctionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupSearchBar()
        setupCategoryChips()
        setupTabLayout()
        setupRecyclerView()
        loadDummyData() // 실제 앱에서는 API 호출 등으로 대체
        filterAndUpdateAuctions()
    }
    
    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                binding.clearSearchButton.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                filterAndUpdateAuctions()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text.clear()
        }
    }
    
    private fun setupCategoryChips() {
        // 카테고리 칩 선택 처리
        binding.chipAll.isChecked = true
        
        binding.categoriesChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> selectedCategory = "전체"
                R.id.chipArt -> selectedCategory = "특허"
                R.id.chipCollectible -> selectedCategory = "게임"
                R.id.chipDigital -> selectedCategory = "캐릭터"
                R.id.chipRare -> selectedCategory = "프랜차이즈"
            }
            filterAndUpdateAuctions()
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position ?: 0
                filterAndUpdateAuctions()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        auctionAdapter = AuctionAdapter()
        binding.auctionRecyclerView.apply {
            adapter = auctionAdapter
            layoutManager = GridLayoutManager(context, 2) // 2열 그리드
        }
        
        auctionAdapter.setOnItemClickListener { auction ->
            Toast.makeText(context, "${auction.title} 선택됨", Toast.LENGTH_SHORT).show()
            // TODO: 경매 아이템 상세 화면으로 이동
        }
    }
    
    private fun loadDummyData() {
        // 실제 앱에서는 API 호출로 데이터를 로드합니다
        val calendar = Calendar.getInstance()
        
        auctionList.clear()
        auctionList.addAll(
            listOf(
                createAuctionItem("1", "우주를 담은 디지털 아트", "디지털아트", 150000, 2, true),
                createAuctionItem("2", "연못 속 수련", "그림", 2500000, 1, false),
                createAuctionItem("3", "일출 풍경 사진", "사진", 350000, 3, false),
                createAuctionItem("4", "한정판 피규어", "수집품", 890000, 1, true),
                createAuctionItem("5", "추상화 작품", "그림", 1200000, 2, false),
                createAuctionItem("6", "도시 야경 사진", "사진", 450000, 4, false),
                createAuctionItem("7", "AI 생성 디지털 아트", "디지털아트", 780000, 5, true),
                createAuctionItem("8", "빈티지 슈퍼히어로 만화책", "수집품", 3500000, 1, false),
                createAuctionItem("9", "자연 풍경화", "그림", 1800000, 2, false),
                createAuctionItem("10", "한정판 음반 컬렉션", "수집품", 650000, 3, true)
            )
        )
    }
    
    private fun createAuctionItem(id: String, title: String, category: String, price: Long, daysToEnd: Int, featured: Boolean): AuctionModel {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysToEnd)
        
        return AuctionModel(
            id = id,
            title = title,
            description = "상세 설명은 실제 API 데이터에서 가져옵니다.",
            imageUrl = "https://picsum.photos/400/400?random=$id",
            currentPrice = price,
            endTime = calendar.time,
            isFeatured = featured,
            category = category
        )
    }
    
    private fun filterAndUpdateAuctions() {
        filteredAuctionList.clear()
        
        // 검색어, 카테고리, 탭에 따라 필터링
        val tempList = auctionList.filter { auction ->
            // 검색어 필터링
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                auction.title.contains(searchQuery, ignoreCase = true) || 
                auction.description.contains(searchQuery, ignoreCase = true)
            } else true
            
            // 카테고리 필터링
            val matchesCategory = if (selectedCategory != "전체") {
                auction.category == selectedCategory
            } else true
            
            // 탭 필터링 (전체/주목/마감임박)
            val now = Date()
            val timeLeftMillis = auction.endTime.time - now.time
            val hoursLeft = timeLeftMillis / (1000 * 60 * 60)
            
            val matchesTab = when (activeTab) {
                0 -> true  // 전체: 모든 경매
                1 -> auction.isFeatured  // 주목: 주목 표시된 경매만
                2 -> hoursLeft < 24  // 마감임박: 24시간 이내 마감
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesTab
        }
        
        filteredAuctionList.addAll(tempList)
        auctionAdapter.submitList(filteredAuctionList)
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.updateHeaderTitle("IP옥션")
        activityViewModel.updateNavigationIcon(R.drawable.ic_arrow_back)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

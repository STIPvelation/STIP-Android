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
import com.stip.stip.databinding.FragmentMoreIpSwapBinding
import com.stip.stip.more.fragment.ipentertainment.adapter.SwapAdapter
import com.stip.stip.more.fragment.ipentertainment.model.SwapModel

class IPSwapFragment : Fragment() {
    private var _binding: FragmentMoreIpSwapBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private val TAG = "IPSwapFragment_Debug"
    
    private lateinit var swapAdapter: SwapAdapter
    private var swapList = mutableListOf<SwapModel>()
    private var filteredSwapList = mutableListOf<SwapModel>()
    
    private var selectedCategory: String = "전체"
    private var activeTab: Int = 0
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreIpSwapBinding.inflate(inflater, container, false)
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
        filterAndUpdateSwaps()
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
                filterAndUpdateSwaps()
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
        
        @Suppress("DEPRECATION")
        binding.categoriesChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> selectedCategory = "전체"
                R.id.chipDigital -> selectedCategory = "디지털"
                R.id.chipPhysical -> selectedCategory = "실물"
                R.id.chipPopular -> selectedCategory = "인기"
                R.id.chipRecent -> selectedCategory = "최신"
            }
            filterAndUpdateSwaps()
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position ?: 0
                filterAndUpdateSwaps()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        swapAdapter = SwapAdapter()
        binding.swapRecyclerView.apply {
            adapter = swapAdapter
            layoutManager = GridLayoutManager(context, 2) // 2열 그리드
        }
        
        swapAdapter.setOnItemClickListener { swap ->
            Toast.makeText(context, "${swap.title} 선택됨", Toast.LENGTH_SHORT).show()
            // TODO: 스왑 아이템 상세 화면으로 이동
        }
    }

    private fun loadDummyData() {
        // 실제 앱에서는 API 호출로 데이터를 로드합니다
        swapList.clear()
        swapList.addAll(
            listOf(
                SwapModel(
                    id = "1",
                    title = "한정판 아트북 컬렉션",
                    description = "유명 일러스트레이터 작품집 10권 세트입니다. 상태 좋음.",
                    imageUrl = "https://picsum.photos/400/400?random=21",
                    wantedItems = listOf("프리미엄 태블릿", "그래픽 카드", "게이밍 모니터"),
                    isPopular = true,
                    category = "실물"
                ),
                SwapModel(
                    id = "2",
                    title = "디지털 아트워크 NFT",
                    description = "제 개인 작품 NFT입니다. 세계적인 NFT 거래소에 등록된 작품입니다.",
                    imageUrl = "https://picsum.photos/400/400?random=22",
                    wantedItems = listOf("다른 NFT 작품", "디지털 아트"),
                    category = "디지털"
                ),
                SwapModel(
                    id = "3",
                    title = "빈티지 피규어 세트",
                    description = "90년대 한정판 피규어 5개입니다. 박스 포함 미개봉 상태.",
                    imageUrl = "https://picsum.photos/400/400?random=23",
                    wantedItems = listOf("현대 아트 토이", "레고 한정판", "빈티지 게임기"),
                    isPopular = true,
                    category = "수집품"
                ),
                SwapModel(
                    id = "4",
                    title = "디지털 음원 소스 파일",
                    description = "프로듀싱한 음원 소스 파일 30개입니다. 상업적 이용 가능.",
                    imageUrl = "https://picsum.photos/400/400?random=24",
                    wantedItems = listOf("믹싱 플러그인", "미디 컨트롤러"),
                    category = "디지털"
                ),
                SwapModel(
                    id = "5",
                    title = "자작 아트 키보드",
                    description = "직접 디자인하고 조립한 기계식 키보드입니다. 체리 청축 사용.",
                    imageUrl = "https://picsum.photos/400/400?random=25",
                    wantedItems = listOf("프리미엄 마우스", "게이밍 헤드셋", "오디오 인터페이스"),
                    category = "실물"
                ),
                SwapModel(
                    id = "6",
                    title = "프로그래밍 튜토리얼 구독권",
                    description = "온라인 코딩 강의 1년 구독권입니다. 모든 과정 이용 가능.",
                    imageUrl = "https://picsum.photos/400/400?random=26",
                    wantedItems = listOf("디자인 툴 라이센스", "클라우드 서비스 크레딧"),
                    isPopular = false,
                    category = "디지털"
                ),
                SwapModel(
                    id = "7",
                    title = "캘리그라피 원본 작품",
                    description = "직접 작업한 캘리그라피 원본 작품 5점입니다.",
                    imageUrl = "https://picsum.photos/400/400?random=27",
                    wantedItems = listOf("고급 붓", "수채화 도구 세트", "일러스트 작품"),
                    isPopular = true,
                    category = "실물"
                ),
                SwapModel(
                    id = "8",
                    title = "희귀 영화 포스터 컬렉션",
                    description = "80-90년대 개봉한 영화의 초판 포스터 10종입니다.",
                    imageUrl = "https://picsum.photos/400/400?random=28",
                    wantedItems = listOf("음반 컬렉션", "영화 소품"),
                    category = "수집품"
                )
            )
        )
    }
    
    private fun filterAndUpdateSwaps() {
        filteredSwapList.clear()
        
        // 검색어, 카테고리, 탭에 따라 필터링
        val tempList = swapList.filter { swap ->
            // 검색어 필터링
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                swap.title.contains(searchQuery, ignoreCase = true) || 
                swap.description.contains(searchQuery, ignoreCase = true)
            } else true
            
            // 카테고리 필터링
            val matchesCategory = if (selectedCategory != "전체") {
                swap.category == selectedCategory
            } else true
            
            // 탭 필터링 (전체/인기/신규)
            val matchesTab = when (activeTab) {
                0 -> true  // 전체: 모든 스왑 아이템
                1 -> swap.isPopular  // 인기: 인기 표시된 아이템만
                2 -> swap.id.toInt() > swapList.size - 3  // 신규: 가장 최근에 추가된 항목들 (임시 구현)
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesTab
        }
        
        filteredSwapList.addAll(tempList)
        swapAdapter.submitList(filteredSwapList)
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.updateHeaderTitle("IP스왑")
        activityViewModel.updateNavigationIcon(R.drawable.ic_arrow_back)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

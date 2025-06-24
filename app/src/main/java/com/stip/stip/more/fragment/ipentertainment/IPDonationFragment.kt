package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreIpDonationBinding
import com.stip.stip.more.fragment.ipentertainment.adapter.DonationAdapter
import com.stip.stip.more.fragment.ipentertainment.model.DonationModel
import java.util.*

class IPDonationFragment : Fragment() {
    private var _binding: FragmentMoreIpDonationBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private val TAG = "IPDonationFragment_Debug"
    
    private lateinit var donationAdapter: DonationAdapter
    private var donationList = mutableListOf<DonationModel>()
    private var filteredDonationList = mutableListOf<DonationModel>()
    
    private var selectedCategory: String = "전체"
    private var activeTab: Int = 0
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreIpDonationBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // 옵션 메뉴 활성화
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
        filterAndUpdateDonations()
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
                filterAndUpdateDonations()
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
                R.id.chipEducation -> selectedCategory = "교육"
                R.id.chipOngoing -> selectedCategory = "진행중"
                R.id.chipUrgent -> selectedCategory = "긴급"
                R.id.chipNonprofit -> selectedCategory = "비영리"
            }
            filterAndUpdateDonations()
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                activeTab = tab?.position ?: 0
                filterAndUpdateDonations()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        donationAdapter = DonationAdapter()
        binding.donationRecyclerView.apply {
            adapter = donationAdapter
            layoutManager = GridLayoutManager(context, 2) // 2열 그리드
        }
        
        donationAdapter.setOnItemClickListener { donation ->
            Toast.makeText(context, "${donation.title} 선택됨", Toast.LENGTH_SHORT).show()
            // TODO: 도네이션 상세 화면으로 이동
        }
    }

    private fun loadDummyData() {
        // 실제 앱에서는 API 호출로 데이터를 로드합니다
        donationList.clear()
        
        val calendar = Calendar.getInstance()
        
        // 끝나는 날짜 설정을 위한 포맷팅
        val endDate1 = calendar.clone() as Calendar
        endDate1.add(Calendar.DAY_OF_MONTH, 15) // 15일 후
        
        val endDate2 = calendar.clone() as Calendar
        endDate2.add(Calendar.DAY_OF_MONTH, 7) // 7일 후
        
        val endDate3 = calendar.clone() as Calendar
        endDate3.add(Calendar.DAY_OF_MONTH, 30) // 30일 후
        
        val endDate4 = calendar.clone() as Calendar
        endDate4.add(Calendar.DAY_OF_MONTH, 5) // 5일 후
        
        val endDate5 = calendar.clone() as Calendar
        endDate5.add(Calendar.DAY_OF_MONTH, 45) // 45일 후
        
        donationList.addAll(
            listOf(
                DonationModel(
                    id = "1",
                    title = "교육 기회 확대를 위한 도서 기부 프로젝트",
                    organizer = "국제 교육 재단",
                    description = "교육 소외 지역 아동들을 위한 도서 구매 프로젝트입니다. 기부해주신 도서는 전국 어린이도서관에 배분됩니다.",
                    imageUrl = "https://picsum.photos/400/400?random=31",
                    currentAmount = 3250000,
                    goalAmount = 5000000,
                    endDate = endDate1.time,
                    isUrgent = false,
                    category = "교육"
                ),
                DonationModel(
                    id = "2",
                    title = "환경보호 작은 그림 프로젝트",
                    organizer = "지구사랑연합",
                    description = "환경 오염 실태를 알리기 위한 아동 그림책 출판 프로젝트입니다.",
                    imageUrl = "https://picsum.photos/400/400?random=32",
                    currentAmount = 1800000,
                    goalAmount = 2500000,
                    endDate = endDate2.time,
                    isUrgent = true,
                    category = "환경"
                )
            )
        )
        
        // 추가 데이터 로드
        donationList.add(
            DonationModel(
                id = "3",
                title = "의료 소외 지역 중학생 건강검진 지원",
                organizer = "건강 지키기 재단",
                description = "의료 사각지역의 중학생들에게 무료 건강검진을 제공하는 프로젝트입니다.",
                imageUrl = "https://picsum.photos/400/400?random=33",
                currentAmount = 12500000,
                goalAmount = 15000000,
                endDate = endDate3.time,
                isUrgent = false,
                category = "의료"
            )
        )
        
        donationList.add(
            DonationModel(
                id = "4",
                title = "집중 호우 피해 지역 지원 프로젝트",
                organizer = "재난구호연합",
                description = "호우 피해로 어려움을 겨융하는 지역사회의 재건축을 위한 긴급 프로젝트입니다.",
                imageUrl = "https://picsum.photos/400/400?random=34",
                currentAmount = 4750000,
                goalAmount = 5000000,
                endDate = endDate4.time,
                isUrgent = true,
                category = "의료"
            )
        )
        
        donationList.add(
            DonationModel(
                id = "5",
                title = "소외 지역 문화공간 조성 프로젝트",
                organizer = "문화 다모임",
                description = "문화 소외 지역에 작은 문화 공간을 조성하여 예술 활동을 지원합니다.",
                imageUrl = "https://picsum.photos/400/400?random=35",
                currentAmount = 8500000,
                goalAmount = 12000000,
                endDate = endDate5.time,
                isUrgent = false,
                category = "문화예술"
            )
        )
    }
        
    private fun filterAndUpdateDonations() {
        filteredDonationList.clear()
        
        // 검색어, 카테고리, 탭에 따라 필터링
        val tempList = donationList.filter { donation ->
            // 검색어 필터링
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                donation.title.contains(searchQuery, ignoreCase = true) || 
                donation.description.contains(searchQuery, ignoreCase = true) ||
                donation.organizer.contains(searchQuery, ignoreCase = true)
            } else true
            
            // 카테고리 필터링
            val matchesCategory = if (selectedCategory != "전체") {
                donation.category == selectedCategory
            } else true
            
            // 탭 필터링 (전체/긴급/인기)
            val matchesTab = when (activeTab) {
                0 -> true  // 전체: 모든 기부 프로젝트
                1 -> donation.isUrgent  // 긴급: 긴급한 프로젝트만
                2 -> {
                    // 인기: 목표액 대비 진행률이 높은 순으로
                    val progress = donation.getProgress()
                    progress > 60 // 60% 이상 진행된 프로젝트를 인기라고 간주
                }
                else -> true
            }
            
            matchesSearch && matchesCategory && matchesTab
        }
        
        filteredDonationList.addAll(tempList)
        donationAdapter.submitList(filteredDonationList)
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.updateHeaderTitle("IP기부")
        activityViewModel.updateNavigationIcon(R.drawable.ic_arrow_back)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}

package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
// TabLayout 제거로 import 제거
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreIpAuctionBinding
import com.stip.stip.more.fragment.ipentertainment.AuctionDetailFragment
import com.stip.stip.more.fragment.ipentertainment.adapter.AuctionAdapter
import com.stip.stip.more.fragment.ipentertainment.data.AuctionModel
import com.stip.stip.more.fragment.ipentertainment.data.IpType
import java.util.*

class IPAuctionFragment : Fragment() {
    private var _binding: FragmentMoreIpAuctionBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()
    private val TAG = "IPAuctionFragment_Debug"

    private lateinit var auctionAdapter: AuctionAdapter
    private var auctionList = mutableListOf<AuctionModel>()
    private var filteredAuctionList = mutableListOf<AuctionModel>()
    
    private var selectedCategory: String = "전체" // 카테고리 버튼에 사용되며 나중에 연결
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
        setupSearchBar()
        // setupTabLayout() - 탭 레이아웃 삭제로 호출 제거
        setupRecyclerView()
        loadDummyData() // 실제 앱에서는 API 호출 등으로 대체
        
        // 데이터가 있는지 로그로 확인
        Log.d(TAG, "Loaded auction items: ${auctionList.size}")
        for (item in auctionList) {
            Log.d(TAG, "Item: ${item.title}, Type: ${item.ipType.displayName}")
        }
        
        filterAndUpdateAuctions()
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
    
    // TabLayout 제거로 setupTabLayout() 함수도 제거됨
    
    private fun setupRecyclerView() {
        // RecyclerView 가시성 보장
        binding.auctionRecyclerView.apply {
            visibility = View.VISIBLE
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        
        // 어댑터 초기화 및 설정
        auctionAdapter = AuctionAdapter().apply {
            setOnItemClickListener { auction, position ->
                // 클릭 시 상세 페이지로 이동
                Log.d(TAG, "Clicked auction: ${auction.title} at position $position")
                navigateToAuctionDetail(auction)
            }
        }
        binding.auctionRecyclerView.adapter = auctionAdapter
        
        
        Log.d(TAG, "RecyclerView 설정 완료")
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
        
        // IP 유형 랜덤 할당
        val ipTypes = IpType.values()
        val randomIpType = ipTypes[id.toInt() % ipTypes.size]
        
        // 기존 아이템에 새 필드 추가
        return AuctionModel(
            id = id,
            title = title,
            description = "상세 설명은 실제 API 데이터에서 가져옵니다.",
            imageUrl = "https://picsum.photos/400/400?random=$id",
            startPrice = price * 8 / 10, // 시작가는 현재가의 80%
            currentPrice = price,
            endTime = calendar.time,
            isFeatured = featured,
            category = category,
            ipType = randomIpType,
            registrationNumber = "C-2023-${10000 + id.toInt()}",
            bidCount = (5..50).random(),
            viewCount = (20..200).random()
        )
    }
    
    private fun filterAndUpdateAuctions() {
        // 검색어에 따라 필터링 (카테고리 버튼은 나중에 구현)
        val tempList = auctionList.filter { auction ->
            // 검색어 필터링
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                auction.title.contains(searchQuery, ignoreCase = true) || 
                auction.description.contains(searchQuery, ignoreCase = true)
            } else true
            
            // TabLayout 삭제로 탭 필터링 제거
            matchesSearch
        }
        
        // 필터링된 리스트 갱신 및 로깅
        filteredAuctionList.clear()
        filteredAuctionList.addAll(tempList)
        
        Log.d(TAG, "Filtered auction items: ${filteredAuctionList.size}")
        
        // ArrayList로 새로 생성하여 전달 (변경사항이 제대로 반영되도록)
        auctionAdapter.submitList(filteredAuctionList)
        
        // 정상적으로 표시되는지 확인하기 위한 지연 후 다시 한번 갱신
        binding.auctionRecyclerView.postDelayed({
            auctionAdapter.notifyDataSetChanged()
        }, 100)
    }

    override fun onResume() {
        super.onResume()
        activityViewModel.updateHeaderTitle("IP옥션")
        activityViewModel.updateNavigationIcon(R.drawable.ic_arrow_back)
        activityViewModel.updateNavigationClickListener {
            // Navigate back to MoreFragment using fragment transaction back stack
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun navigateToAuctionDetail(auction: AuctionModel) {
        // 디테일 화면으로 넘겨줘야 할 데이터를 Bundle에 담아서 전달
        val bundle = Bundle().apply {
            putString("id", auction.id)
            putString("title", auction.title)
            putString("description", auction.description)
            putString("imageUrl", auction.imageUrl)
            putLong("startPrice", auction.startPrice)
            putLong("currentPrice", auction.currentPrice)
            putLong("endTime", auction.endTime.time)
            putInt("bidCount", auction.bidCount)
            putString("ipType", auction.ipType.name)
            putBoolean("isFeatured", auction.isFeatured)
            putString("registrationNumber", auction.registrationNumber)
            putInt("viewCount", auction.viewCount)
        }
        
        // 프래그먼트 트랜잭션을 통해 디테일 화면으로 이동
        val fragmentManager = parentFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AuctionDetailFragment().apply { 
                arguments = bundle 
            })
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

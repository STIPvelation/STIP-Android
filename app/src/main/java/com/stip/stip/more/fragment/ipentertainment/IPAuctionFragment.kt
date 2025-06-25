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
        // setupBackButton() - 삭제: btnBack이 레이아웃에 존재하지 않음
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
    
    // btnBack 버튼이 레이아웃에 존재하지 않아 setupBackButton() 메소드 삭제
    

    
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
                createAuctionItem(
                    "1", 
                    "우주 탐험 디지털 아트 시리즈", 
                    "디지털아트", 
                    150, 
                    2, 
                    true,
                    "디지털 우주 탐험 시리즈 중 첫 번째 작품으로, 독점 NFT 기술로 제작된 우주 탐사선의 미래적 예술 표현입니다. 블록체인 인증 포함.",
                    "DMCA-2023-10001"
                ),
                createAuctionItem(
                    "2", 
                    "모네 스타일 연못 속 수련", 
                    "그림", 
                    2500, 
                    1, 
                    false,
                    "현대적 기법으로 재해석한 모네 스타일의 수련 그림. 디지털과 아날로그 기법을 혼합한 혁신적 작품으로 한정판 아트 프린트 1/10.",
                    "C-ARTS-2023-10002"
                ),
                createAuctionItem(
                    "3", 
                    "히말라야 일출 사진 저작권", 
                    "사진", 
                    350, 
                    3, 
                    false,
                    "내셔널 지오그래픽에 게재된 히말라야 일출 사진의 상업적 사용권. 고해상도 마스터 파일과 인쇄권 포함.",
                    "P-2023-10003"
                ),
                createAuctionItem(
                    "4", 
                    "전설의 검사 피규어 상표권", 
                    "수집품", 
                    890, 
                    1, 
                    true,
                    "인기 게임 '전설의 검사' 캐릭터의 피규어 제작 상표권. 3D 모델링 파일과 북미 지역 5년 제조 라이선스 포함.",
                    "TM-2023-10004"
                ),
                createAuctionItem(
                    "5", 
                    "뉴욕 현대미술관 추상화 저작권", 
                    "그림", 
                    1200, 
                    2, 
                    false,
                    "뉴욕 현대미술관에 전시된 추상화 작품의 디지털 복제 및 상품화 권한. 전 세계 상품화 권한 2년 라이선스.",
                    "FA-2023-10005"
                ),
                createAuctionItem(
                    "6", 
                    "동경 야경 파노라마 특허", 
                    "사진", 
                    450, 
                    4, 
                    false,
                    "특허 받은 야경 촬영 기술로 제작된 동경 360도 파노라마 이미지의 독점 사용권. 메타버스 환경 활용 가능.",
                    "PT-2023-10006"
                ),
                createAuctionItem(
                    "7", 
                    "AI 아트 생성 알고리즘 특허", 
                    "디지털아트", 
                    780, 
                    5, 
                    true,
                    "혁신적인 AI 아트 생성 알고리즘의 사용 라이선스. 상업적 작품 제작에 활용 가능한 전문 인공지능 모델 포함.",
                    "AI-PT-2023-10007"
                ),
                createAuctionItem(
                    "8", 
                    "슈퍼히어로 1판 만화책 저작권", 
                    "수집품", 
                    3500, 
                    1, 
                    false,
                    "1960년대 출판된 희귀 슈퍼히어로 만화의 캐릭터 활용 저작권. 영화, 게임, 상품 제작권 포함(북미 지역 한정).",
                    "CC-1960-10008"
                ),
                createAuctionItem(
                    "9", 
                    "제주 풍경화 NFT 원본", 
                    "그림", 
                    1800, 
                    2, 
                    false,
                    "한국 전통 기법과 현대적 디지털 기술을 결합한 제주 풍경화 NFT. 실물 작품 소유권과 디지털 인증서 포함.",
                    "K-NFT-2023-10009"
                ),
                createAuctionItem(
                    "10", 
                    "전설적 밴드 미발매곡 저작권", 
                    "수집품", 
                    650, 
                    3, 
                    true,
                    "세계적인 록 밴드의 미발매 음원 5곡의 독점 소유권 및 제한적 상업 이용권. 마스터 녹음본 포함.",
                    "MR-2023-10010"
                )
            )
        )
    }
    
    private fun createAuctionItem(
        id: String, 
        title: String, 
        category: String, 
        price: Long, 
        daysToEnd: Int, 
        featured: Boolean,
        description: String = "상세 설명은 실제 API 데이터에서 가져옵니다.",
        regNumber: String = "C-2023-${10000 + id.toInt()}"
    ): AuctionModel {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysToEnd)
        
        // IP 유형 랜덤 할당
        val ipTypes = IpType.values()
        val randomIpType = ipTypes[id.toInt() % ipTypes.size]
        
        // 달러 기반 가격으로 변경 (기존 가격을 그대로 사용 - 이미 달러로 변경됨)
        return AuctionModel(
            id = id,
            title = title,
            description = description,
            imageUrl = "https://picsum.photos/400/400?random=$id",
            startPrice = price * 8 / 10, // 시작가는 현재가의 80%
            currentPrice = price,
            endTime = calendar.time,
            isFeatured = featured,
            category = category,
            ipType = randomIpType,
            registrationNumber = regNumber,
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

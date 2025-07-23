package com.stip.stip.iptransaction.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.ScrollableToTop
import com.stip.stip.iptransaction.adapter.IpInvestmentAdapter
import com.stip.stip.databinding.FragmentIpInvestmentBinding
import com.stip.stip.iptransaction.api.IpTransactionService
import com.stip.stip.iptransaction.model.IpInvestmentItem
import com.stip.stip.signup.utils.PreferenceUtil
import com.stip.stip.iptransaction.fragment.TickerSelectionDialogFragment
import com.stip.api.repository.WalletHistoryRepository
import com.stip.api.model.WalletHistoryRecord
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.text.NumberFormat
import java.util.Locale
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.stip.stip.api.RetrofitClient
import com.stip.stip.api.service.MarketPairsService

class IpInvestmentFragment : Fragment(), ScrollableToTop, TickerSelectionDialogFragment.TickerSelectionListener {

    private lateinit var mainViewModel: com.stip.stip.MainViewModel

    private var _binding: FragmentIpInvestmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var ipInvestmentAdapter: IpInvestmentAdapter
    private val walletHistoryRepository = WalletHistoryRepository()
    
    // 현재 선택된 날짜 범위를 저장하기 위한 변수
    private var currentStartDate: String = ""
    private var currentEndDate: String = ""

    private var marketPairMap: Map<String, String> = emptyMap()

    companion object {
        @JvmStatic
        fun newInstance() = IpInvestmentFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpInvestmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.stip.stip.MainViewModel::class.java]
        mainViewModel.memberInfo.observe(viewLifecycleOwner) { memberInfo ->
            // TODO: 회원정보를 UI에 반영하는 코드 작성
        }

        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupScrollSync()
        // 마켓 페어 매핑 먼저 불러온 후 거래내역 로드 (1개월 기간으로 초기 로드)
        viewLifecycleOwner.lifecycleScope.launch {
            marketPairMap = fetchMarketPairs()
            // 1개월 기간으로 초기 데이터 로드
            val endDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val startDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(
                java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -1) }.time
            )
            currentStartDate = startDate
            currentEndDate = endDate
            // 필터 라벨을 1개월로 설정
            binding.textViewFilterLabel.text = "1개월 내역보기"
            loadAllData(null, startDate, endDate)
        }

        binding.imageViewFilterIcon.setOnClickListener {
            navigateToInvestmentFilter()
        }

        binding.buttonIpSearch.setOnClickListener {
            showTickerSelectionDialog()
        }

        parentFragmentManager.setFragmentResultListener("investmentFilterResult", viewLifecycleOwner) { _, bundle ->
            val types = bundle.getStringArrayList("filterTypes") ?: arrayListOf()
            val startDate = bundle.getString("filterStartDate")
            val endDate = bundle.getString("filterEndDate")
            val periodLabel = bundle.getString("filterPeriodLabel") ?: ""
            binding.textViewFilterLabel.text = "$periodLabel 내역보기"
            viewLifecycleOwner.lifecycleScope.launch {
                marketPairMap = fetchMarketPairs()
                loadAllData(types, startDate, endDate)
            }
        }
    }

    private fun setupRecyclerView() {
        ipInvestmentAdapter = IpInvestmentAdapter()
        binding.recyclerViewInvestmentList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ipInvestmentAdapter
            val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            ContextCompat.getDrawable(requireContext(), R.drawable.recycler_divider)?.let {
                divider.setDrawable(it)
                addItemDecoration(divider)
            }
        }
    }

    private fun setupScrollSync() {
        binding.headerScrollView.bindSyncTarget(binding.scrollableContent)
        binding.scrollableContent.bindSyncTarget(binding.headerScrollView)
    }

    private fun loadAllData(
        filterTypes: List<String>? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        // 로그인 여부 확인
        if (PreferenceUtil.getToken().isNullOrEmpty()) {
            showEmptyState("로그인이 필요합니다.")
            return
        }
        
        // 현재 선택된 날짜 범위를 저장
        currentStartDate = startDate ?: ""
        currentEndDate = endDate ?: ""
        
        // 로딩 상태 표시
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewInvestmentList.visibility = View.GONE
        binding.noDataContainer.visibility = View.GONE
        
        // 입출금 내역과 매수/매도 내역을 모두 로드
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val memberId = PreferenceUtil.getUserId()
                if (memberId.isNullOrBlank()) {
                    showEmptyState("사용자 정보를 찾을 수 없습니다.")
                    return@launch
                }
                
                // 매수/매도 내역 로드
                val buySellItems = loadBuySellData(memberId)
                
                // 필터 타입에 따른 데이터 필터링
                val filteredItems = if (!filterTypes.isNullOrEmpty()) {
                    val allItems = mutableListOf<IpInvestmentItem>()
                    
                    // 필터 타입에 따라 데이터 추가
                    filterTypes.forEach { filterType ->
                        when (filterType) {
                            "매수", "매도" -> {
                                allItems.addAll(buySellItems.filter { it.type == filterType })
                            }
                        }
                    }
                    allItems
                } else {
                    // 필터가 없는 경우 모든 데이터 합치기
                    buySellItems
                }
                
                // 날짜순으로 정렬 (최신순)
                val sortedItems = filteredItems.sortedByDescending { item ->
                    try {
                        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        dateFormat.parse(item.orderTime)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                
                // UI 스레드에서 처리
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    if (sortedItems.isNotEmpty()) {
                        showInvestmentData(sortedItems)
                    } else {
                        showEmptyState("해당 기간의 거래내역이 없습니다.")
                    }
                }
            } catch (e: Exception) {
                Log.e("IpInvestmentFragment", "데이터 로드 실패", e)
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    showEmptyState("거래내역을 불러오는 중 오류가 발생했습니다.")
                }
            }
        }
    }
    

    
    private suspend fun loadBuySellData(memberId: String): List<IpInvestmentItem> {
        return try {
            // 기간 필터가 있는 경우 기간별 주문 내역 조회 API 사용
            val apiOrders = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val orders = mutableListOf<com.stip.stip.iptransaction.model.ApiOrderResponse>()
                val latch = java.util.concurrent.CountDownLatch(1)
                var error: Throwable? = null
                
                if (currentStartDate.isNotEmpty() && currentEndDate.isNotEmpty()) {
                    // 기간별 주문 내역 조회 API 호출
                    Log.d("IpInvestmentFragment", "기간별 주문 내역 조회: $currentStartDate ~ $currentEndDate")
                    IpTransactionService.getOrdersByDateRange(currentStartDate, currentEndDate) { orderListResponse, err ->
                        if (err != null) {
                            error = err
                        } else {
                            orderListResponse?.data?.let { orders.addAll(it) }
                        }
                        latch.countDown()
                    }
                } else {
                    // 기간 필터가 없는 경우 기존 API 사용
                    Log.d("IpInvestmentFragment", "전체 주문 내역 조회")
                    IpTransactionService.getApiFilledOrders(memberId) { apiOrders, err ->
                        if (err != null) {
                            error = err
                        } else {
                            apiOrders?.let { orders.addAll(it) }
                        }
                        latch.countDown()
                    }
                }
                
                latch.await()
                if (error != null) throw error!!
                orders
            }
            
            Log.d("IpInvestmentFragment", "매수/매도 내역 API 응답: ${apiOrders.size}개")
            
            // API 응답을 IpInvestmentItem으로 변환
            apiOrders.map { apiOrder ->
                val type = if (apiOrder.type == "buy") "매수" else "매도"
                val orderTime = formatDateTime(apiOrder.createdAt)
                val executionTime = formatDateTime(apiOrder.updatedAt)
                val symbol = apiOrder.marketPair.symbol
                val baseAsset = symbol.substringBefore("/")
                
                IpInvestmentItem(
                    type = type,
                    name = baseAsset,
                    quantity = apiOrder.filledQuantity.toString(),
                    unitPrice = String.format("%.2f", apiOrder.price),
                    amount = String.format("%.2f", apiOrder.price * apiOrder.filledQuantity),
                    fee = "0.00",
                    settlement = String.format("%.2f", apiOrder.price * apiOrder.filledQuantity),
                    orderTime = orderTime,
                    executionTime = executionTime
                )
            }
        } catch (e: Exception) {
            Log.e("IpInvestmentFragment", "매수/매도 내역 로드 실패", e)
            emptyList()
        }
    }
    
    private fun showEmptyState(message: String = "해당 기간의 거래내역이 없습니다.") {
        binding.recyclerViewInvestmentList.visibility = View.GONE
        binding.noDataContainer.visibility = View.VISIBLE
        binding.noDataText.text = message
        binding.periodText.visibility = View.VISIBLE
        
        // 필터에서 선택한 기간에 따라 표시
        if (currentStartDate.isNotEmpty() && currentEndDate.isNotEmpty()) {
            binding.periodText.text = "$currentStartDate ~ $currentEndDate"
        } else {
            // 선택된 필터가 없을 경우 기본값 표시
            binding.periodText.text = binding.textViewFilterLabel.text.toString()
        }
        
        ipInvestmentAdapter.updateData(emptyList())
    }
    
    private fun showInvestmentData(data: List<IpInvestmentItem>) {
        if (data.isNotEmpty()) {
            binding.recyclerViewInvestmentList.visibility = View.VISIBLE
            binding.noDataContainer.visibility = View.GONE
            ipInvestmentAdapter.updateData(data)
        } else {
            showEmptyState()
        }
    }

    private fun navigateToInvestmentFilter() {
        val filterFragment = InvestmentFilterFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, filterFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun scrollToTop() {
        binding.recyclerViewInvestmentList.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * 티커 선택 다이얼로그를 표시하는 메소드
     */
    private fun showTickerSelectionDialog() {
        val dialogFragment = TickerSelectionDialogFragment.newInstance(this)
        dialogFragment.show(parentFragmentManager, "TickerSelectionDialogFragment")
    }
    
    /**
     * 티커 선택 리스너 구현
     */
    override fun onTickerSelected(ticker: String) {
        // 선택된 티커로 버튼 텍스트 업데이트
        binding.buttonIpSearch.text = ticker
        
        // "전체"가 선택된 경우 필터링 없이 모든 데이터 로드
        if (ticker == "전체") {
            loadAllData(null)
        } else {
            // 선택된 티커로 데이터 필터링
            val filterTypes = listOf(ticker)
            loadAllData(filterTypes)
        }
    }
    

    
    private fun parseIsoTimestampToMillis(iso: String): Long {
        return try {
            val fixedIso = iso.replace(Regex("\\.(\\d{3})\\d{3}"), ".$1")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getDefault()
            dateFormat.parse(fixedIso)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("IpInvestmentFragment", "timestamp 파싱 실패: $iso", e)
            System.currentTimeMillis()
        }
    }
    
    private fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            Log.w("IpInvestmentFragment", "Failed to parse date: $dateString", e)
            "00:00:00"
        }
    }

    // 마켓 페어 매핑 불러오기
    private suspend fun fetchMarketPairs(): Map<String, String> {
        return try {
            val api = RetrofitClient.createTapiService(MarketPairsService::class.java)
            val response = api.getMarketPairs()
            response.associate { it.id to it.symbol }
        } catch (e: Exception) {
            Log.e("IpInvestmentFragment", "마켓 페어 매핑 불러오기 실패", e)
            emptyMap()
        }
    }
}
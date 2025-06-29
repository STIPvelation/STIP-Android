package com.stip.ipasset.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.stip.stip.MainActivity
import com.stip.dummy.AssetDummyData
import com.stip.ipasset.ticker.fragment.TickerTransactionFragment
import com.stip.ipasset.usd.fragment.USDDepositFragment
import com.stip.ipasset.usd.manager.USDAssetManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetBinding
import com.stip.stip.databinding.ItemTickerAssetBinding
import com.stip.stip.databinding.ItemUsdAssetBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class IpAssetFragment : Fragment() {

    private var _binding: FragmentIpAssetBinding? = null
    private val binding get() = _binding!!
    
    // USD 자산 매니저
    private val assetManager = USDAssetManager.getInstance()
    
    private lateinit var adapter: IpAssetsAdapter
    private val assetsList = mutableListOf<IpAssetItem>()
    private val filteredList = mutableListOf<IpAssetItem>()
    private var currentFilter = FilterType.ALL
    
    // 필터 타입 정의
    enum class FilterType {
        ALL, HOLDING
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // USD 데이터 변경 관찰
        observeUsdData()
        
        // KRW 입금 버튼 클릭 시 USDDepositFragment로 이동
        binding.buttonKrwDeposit.setOnClickListener {
            try {
                // 프래그먼트 트랜잭션을 사용하여 USDDepositFragment로 교체
                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                
                // 애니메이션 추가
                fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,  // 들어오는 애니메이션 (없으면 생성 필요)
                    R.anim.slide_out_left,   // 나가는 애니메이션 (없으면 생성 필요)
                    R.anim.slide_in_left,    // 들어오는 애니메이션 (백스택에서 돌아올 때)
                    R.anim.slide_out_right   // 나가는 애니메이션 (백스택에서 돌아올 때)
                )
                
                // 새로운 USDDepositFragment 인스턴스 생성
                val usdDepositFragment = USDDepositFragment()
                
                // 프래그먼트 교체 및 백스택에 추가
                fragmentTransaction.replace(R.id.fragment_container, usdDepositFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
                
            } catch (e: Exception) {
                // 오류 발생 시 메시지 표시
                Toast.makeText(requireContext(), "화면 전환 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        setupRecyclerView()
        setupDummyData()
        setupSearchAndFilter()
        setupSwipeRefresh()
    }
    
    private fun setupRecyclerView() {
        adapter = IpAssetsAdapter(this)
        binding.ipAssets.layoutManager = LinearLayoutManager(requireContext())
        binding.ipAssets.adapter = adapter
    }
    
    /**
     * USDAssetManager로부터 USD 데이터 변화 관찰
     */
    private fun observeUsdData() {
        assetManager.balance.observe(viewLifecycleOwner) { balance ->
            // USD 자산 아이템 업데이트
            updateUsdAssetItem(balance)
            
            // 직접 총 보유자산 표시 업데이트 (소수점 2자리 고정)
            val totalAssets = assetsList.sumOf { it.usdEquivalent }
            val formatter = java.text.DecimalFormat("#,##0.00")
            binding.totalIpAssets.text = "$${formatter.format(totalAssets)} USD"
            
            android.util.Log.d("IpAssetFragment", "Updated total assets display: ${formatter.format(totalAssets)} USD")
        }
    }
    
    /**
     * USD 자산 업데이트
     */
    private fun updateUsdAssetItem(balance: Double) {
        // USD 데이터 업데이트
        val existingUsdIndex = assetsList.indexOfFirst { it.isUsd }
        
        val usdAssetItem = IpAssetItem(
            currencyCode = "USD",
            amount = balance,
            usdEquivalent = balance,
            krwEquivalent = balance * 1300.0,
            isUsd = true
        )
        
        if (existingUsdIndex >= 0) {
            // 기존 USD 항목 업데이트
            assetsList[existingUsdIndex] = usdAssetItem
        } else {
            // 없으면 추가 (리스트 맨 앞에)
            assetsList.add(0, usdAssetItem)
        }
        
        // 총 자산 계산 및 표시 - 소수점 2자리 고정 포맷
        val totalAssets = assetsList.sumOf { it.usdEquivalent }
        val formatter = java.text.DecimalFormat("#,##0.00")
        binding.totalIpAssets.text = "$${formatter.format(totalAssets)} USD"
        
        // 데이터 변경 알림
        applyFiltering()
    }
    
    private fun setupDummyData() {
        // 기존 리스트 초기화
        assetsList.clear()
        
        // USDAssetManager로부터 USD 데이터 가져오기
        val balance = assetManager.balance.value ?: 0.0
        
        // USD 추가 (항상 최상단에 위치)
        val usdAssetItem = IpAssetItem(
            currencyCode = "USD",
            amount = balance,
            usdEquivalent = balance,
            krwEquivalent = balance * 1300.0,
            isUsd = true
        )
        assetsList.add(usdAssetItem)
        
        // AssetDummyData에서 11개의 티커 자산 가져와서 추가
        val tickerAssets = AssetDummyData.getDefaultAssets().filter { it.ticker != "USD" }
        
        // 11개의 티커 자산 추가
        tickerAssets.forEach { asset ->
            val tickerItem = IpAssetItem(
                currencyCode = asset.ticker,
                amount = asset.balance,
                usdEquivalent = asset.value,
                krwEquivalent = asset.value * 1300.0,  // USD 가격 * 환율
                isUsd = false
            )
            assetsList.add(tickerItem)
        }
        
        // 총 자산 계산 및 표시
        val totalAssets = assetsList.sumOf { it.usdEquivalent }
        val formatter = java.text.DecimalFormat("#,##0.00")
        binding.totalIpAssets.text = "$${formatter.format(totalAssets)} USD"
        
        // 필터링 적용
        applyFiltering()
    }
    
    private fun setupSearchAndFilter() {
        // 검색 기능 - TextWatcher 추가
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                applySearchFilter(s?.toString() ?: "")
            }
        })
        
        // 검색 - 키보드 엔터 이벤트
        binding.searchEditText.setOnEditorActionListener { textView, _, _ ->
            applySearchFilter(textView.text.toString())
            false
        }
        
        // 전체 필터
        binding.filterAll.setOnClickListener {
            currentFilter = FilterType.ALL
            updateFilterButtonUI()
            applyFiltering(binding.searchEditText.text.toString())
        }
        
        // 보유중 필터
        binding.filterHeld.setOnClickListener {
            currentFilter = FilterType.HOLDING
            updateFilterButtonUI()
            applyFiltering(binding.searchEditText.text.toString())
        }
    }
    
    private fun updateFilterButtonUI() {
        if (currentFilter == FilterType.ALL) {
            binding.filterAll.setBackgroundResource(R.drawable.bg_filter_active)
            binding.filterAll.setTextColor(requireContext().getColor(R.color.sky_30C6E8_100))
            binding.filterHeld.setBackgroundResource(R.drawable.bg_filter_inactive)
            binding.filterHeld.setTextColor(requireContext().getColor(android.R.color.darker_gray))
        } else {
            binding.filterHeld.setBackgroundResource(R.drawable.bg_filter_active)
            binding.filterHeld.setTextColor(requireContext().getColor(R.color.sky_30C6E8_100))
            binding.filterAll.setBackgroundResource(R.drawable.bg_filter_inactive)
            binding.filterAll.setTextColor(requireContext().getColor(android.R.color.darker_gray))
        }
    }
    
    private fun applySearchFilter(query: String) {
        applyFiltering(query)
    }
    
    private fun applyFiltering(searchQuery: String = "") {
        // 1. 먼저 USD 항상 포함
        filteredList.clear()
        val usd = assetsList.firstOrNull { it.isUsd }
        
        if (usd != null && (currentFilter == FilterType.ALL || usd.amount > 0)) {
            filteredList.add(usd)
        }
        
        // 2. 검색어와 필터에 맞는 티커들 추가
        val filteredTickers = assetsList
            .filter { !it.isUsd } // USD 아닌 것들만
            .filter { 
                // 검색어 필터링
                if (searchQuery.isNotEmpty()) {
                    it.currencyCode.contains(searchQuery, ignoreCase = true)
                } else true
            }
            .filter {
                // 보유중 필터링
                if (currentFilter == FilterType.HOLDING) it.amount > 0 else true 
            }
            .sortedBy { it.currencyCode } // 알파벳 순 정렬
        
        filteredList.addAll(filteredTickers)
        
        // 3. 결과 적용
        adapter.submitList(filteredList.toList())
    }
    
    private fun setupSwipeRefresh() {
        // 스와이프 리프레시 설정
        binding.swipeRefreshLayout.setOnRefreshListener {
            // 데이터 새로고침 시뮬레이션
            // 실제로는 API 호출을 통해 데이터를 불러오면 됩니다
            setupDummyData()
            
            // 2초 후 리프레시 종료
            Handler(Looper.getMainLooper()).postDelayed({
                if (binding.swipeRefreshLayout.isRefreshing) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }, 2000) // 2초 후 리프레시 인디케이터 중지
        }
        
        // 리프레시 색상 설정
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.sky_30C6E8_100
        )
    }
    
    override fun onResume() {
        super.onResume()
        // 입출금 헤더 텍스트 설정
        (activity as? MainActivity)?.setHeaderTitle("입출금")
        
        // 헤더 레이아웃 표시
        val headerLayout = requireActivity().findViewById<View>(R.id.headerLayout)
        headerLayout?.visibility = View.VISIBLE
        
        // 헤더 타이틀 표시
        val headerTitle = requireActivity().findViewById<TextView>(R.id.headerTitle)
        headerTitle?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
        companion object {
        fun newInstance() = IpAssetFragment()
    }
}

// 자산 데이터 클래스
data class IpAssetItem(
    val currencyCode: String,
    val amount: Double,
    val usdEquivalent: Double,
    val krwEquivalent: Double,
    val isUsd: Boolean
)

// 리사이클러뷰 어댑터
class IpAssetsAdapter(private val fragment: Fragment) : ListAdapter<IpAssetItem, RecyclerView.ViewHolder>(AssetDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USD = 0
        private const val VIEW_TYPE_TICKER = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUsd) VIEW_TYPE_USD else VIEW_TYPE_TICKER
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USD -> {
                val binding = ItemUsdAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                UsdAssetViewHolder(binding)
            }
            else -> {
                val binding = ItemTickerAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TickerViewHolder(binding)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is UsdAssetViewHolder -> holder.bind(item)
            is TickerViewHolder -> holder.bind(item)
        }
    }
    
    // USD 뷰홀더
    inner class UsdAssetViewHolder(private val binding: ItemUsdAssetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IpAssetItem) {
            binding.name.text = item.currencyCode
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            binding.amount.text = formatter.format(item.amount)
            
            // USD 클릭 시 USDTransactionFragment로 이동
            binding.root.setOnClickListener {
                try {
                    // fragment 인스턴스를 사용
                    val fragmentManager = fragment.requireActivity().supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    
                    // 애니메이션 추가
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    
                    // 상위 액티비티의 헤더 숨기기
                    val headerLayout = fragment.requireActivity().findViewById<View>(R.id.headerLayout)
                    headerLayout?.visibility = View.GONE
                    
                    // 새 USDTransactionFragment 인스턴스 생성
                    val transactionFragment = com.stip.ipasset.usd.fragment.USDTransactionFragment()
                    
                    // Bundle에 USD 데이터 전달
                    val bundle = Bundle().apply {
                        putString("currencyCode", item.currencyCode)
                        putDouble("amount", item.amount)
                    }
                    transactionFragment.arguments = bundle
                    
                    // 프래그먼트 교체 및 백스택에 추가
                    fragmentTransaction.replace(R.id.fragment_container, transactionFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                } catch (e: Exception) {
                    Toast.makeText(fragment.requireContext(), "화면 전환 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // 티커 뷰홀더
    inner class TickerViewHolder(private val binding: ItemTickerAssetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IpAssetItem) {
            // 티커 로고 설정
            val tickerInitials = item.currencyCode.take(2)
            binding.tokenLogoText.text = tickerInitials
            
            // 티커 색상 설정
            val context = binding.root.context
            val colorResId = when(item.currencyCode) {
                "JWV" -> R.color.token_jwv
                "MDM" -> R.color.token_mdm
                "CDM" -> R.color.token_cdm
                "IJECT" -> R.color.token_iject
                "WETALK" -> R.color.token_wetalk
                "SLEEP" -> R.color.token_sleep
                "KCOT" -> R.color.token_kcot
                "MSK" -> R.color.token_msk
                "SMT" -> R.color.token_smt
                "AXNO" -> R.color.token_axno
                "KATV" -> R.color.token_katv
                else -> R.color.token_usd
            }
            binding.tokenLogoBackground.backgroundTintList = context.getColorStateList(colorResId)
            
            // 티커 이름과 가격 설정
            binding.name.text = item.currencyCode
            binding.amount.text = String.format(Locale.US, "%.2f", item.amount)
            binding.usdAmount.text = "$${String.format(Locale.US, "%.2f", item.usdEquivalent)}"
            
            // 클릭 이벤트 처리 - 티커 트랜잭션 화면으로 이동
            binding.root.setOnClickListener {
                try {
                    // fragment 인스턴스를 사용
                    val fragmentManager = fragment.requireActivity().supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    
                    // 애니메이션 추가
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    
                    // 상위 액티비티의 헤더 숨기기
                    val headerLayout = fragment.requireActivity().findViewById<View>(R.id.headerLayout)
                    headerLayout?.visibility = View.GONE
                    
                    // 티커 트랜잭션 프래그먼트 생성
                    val tickerTransactionFragment = TickerTransactionFragment.newInstance(
                        tickerCode = item.currencyCode,
                        amount = item.amount,
                        usdEquivalent = item.usdEquivalent
                    )
                    
                    // 프래그먼트 교체 및 백스택에 추가
                    fragmentTransaction.replace(R.id.fragment_container, tickerTransactionFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                } catch (e: Exception) {
                    Toast.makeText(fragment.requireContext(), "화면 전환 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// DiffUtil
class AssetDiffCallback : DiffUtil.ItemCallback<IpAssetItem>() {
    override fun areItemsTheSame(oldItem: IpAssetItem, newItem: IpAssetItem): Boolean {
        return oldItem.currencyCode == newItem.currencyCode
    }
    
    override fun areContentsTheSame(oldItem: IpAssetItem, newItem: IpAssetItem): Boolean {
        return oldItem == newItem
    }
}

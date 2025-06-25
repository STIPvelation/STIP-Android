package com.stip.stip.iphome.fragment

import android.widget.RelativeLayout
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.iphome.adapter.IpListingAdapter
import com.stip.stip.databinding.FragmentIpHomeBinding
import com.stip.stip.iphome.model.IpListingItem
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.MainActivity
import kotlinx.coroutines.launch
import java.util.Locale



class IpHomeFragment : Fragment() {

    private var _binding: FragmentIpHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var ipListingAdapter: IpListingAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var priceUpdateRunnable: Runnable

    private var currentList = mutableListOf<IpListingItem>()
    private var fullList = mutableListOf<IpListingItem>()
    private var isTickerAsc = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadInitialData()
        startAutoPriceUpdate()
        setupSortListeners()
        setupSearchListener()
        setupCategoryDropdown() // 필요 시 이 함수 내부에서 드롭다운 초기화 가능
        setupHeaderTickerSort()
        setLocalizedWatermark()

        // 🔽 드롭다운 필터 적용
        var isDropdownVisible = false

        val frame = binding.frame4043 as RelativeLayout
        frame.setOnClickListener {
            if (isDropdownVisible) {
                collapseDropdown()
            } else {
                expandDropdown()
            }
            isDropdownVisible = !isDropdownVisible
        }
    }

    override fun onResume() {
        super.onResume()
        setupOutsideTouchToCloseDropdown()
        (activity as? MainActivity)?.showHeader() // ✅ 헤더 다시 보이기
    }

    private fun setupOutsideTouchToCloseDropdown() {
        binding.root.setOnTouchListener { _, event ->
            if (binding.categoryDropdown.visibility == View.VISIBLE) {
                val location = IntArray(2)
                binding.frame4043.getLocationOnScreen(location)
                val x = event.rawX
                val y = event.rawY
                val left = location[0]
                val top = location[1]
                val right = left + binding.frame4043.width
                val bottom = top + binding.frame4043.height

                if (x < left || x > right || y < top || y > bottom) {
                    collapseDropdown()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }


    private fun setLocalizedWatermark() {
        val lang = Locale.getDefault().language
        val imageView = binding.watermarkLogo

        val resId = when (lang) {
            "en" -> R.drawable.us_patent_office
            "ja" -> R.drawable.japan_patent_office
            "zh" -> R.drawable.china_patent_office
            else -> R.drawable.kipo_logo // ✅ 기본값: 한국 특허청
        }

        imageView.setImageResource(resId)
    }





    private fun resetTickerSortIcon() {
        val inactive = ContextCompat.getColor(requireContext(), R.color.sort_inactive)

        binding.icTickerSortDownFill.setColorFilter(inactive)
    }











    private fun setupHeaderTickerSort() {
        val active = ContextCompat.getColor(requireContext(), R.color.color_main_point)
        val inactive = ContextCompat.getColor(requireContext(), R.color.sort_inactive)

        binding.sortTickerContainer.setOnClickListener {
            // 정렬 동작
            currentList = if (isTickerAsc) {
                currentList.sortedBy { it.ticker.uppercase() }.toMutableList()
            } else {
                currentList.sortedByDescending { it.ticker.uppercase() }.toMutableList()
            }

            ipListingAdapter.updateItems(currentList)

            // 🔁 화살표 UI 업데이트

            binding.icTickerSortDownFill.setColorFilter(if (isTickerAsc) active else inactive)

            isTickerAsc = !isTickerAsc
        }
    }




    private fun expandDropdown() {
        binding.categoryDropdown.removeAllViews()

        categoryOptions.forEach { category ->
            val item = TextView(requireContext()).apply {
                text = category
                setPadding(24, 12, 24, 12)
                textSize = 12f
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setBackgroundResource(R.drawable.bg_dropdown_item)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setOnClickListener {
                    binding.allIp.text = category
                    currentList = if (category == "ALL IP") {
                        fullList.toMutableList()
                    } else {
                        fullList.filter {
                            it.category.equals(category, ignoreCase = true)
                        }.toMutableList()
                    }
                    ipListingAdapter.updateItems(currentList)
                    collapseDropdown()
                }
            }
            binding.categoryDropdown.addView(item)
        }

        binding.categoryDropdown.visibility = View.VISIBLE
        binding.categoryDropdown.alpha = 0f
        binding.categoryDropdown.animate().alpha(1f).setDuration(200).start()
    }


    private fun collapseDropdown() {
        binding.categoryDropdown.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                binding.categoryDropdown.visibility = View.GONE
            }.start()
    }


    // 드롭다운 열기 / 닫기 toggle
    private fun setupCategoryDropdown() {
        val dropdown = binding.categoryDropdown
        dropdown.removeAllViews() // 혹시 이전 항목이 남아있을 수 있음

        for (option in categoryOptions) {
            val textView = TextView(requireContext()).apply {
                text = option
                setPadding(24, 12, 24, 12)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                textSize = 12f
                setOnClickListener {
                    binding.allIp.text = option
                    dropdown.visibility = View.GONE

                    currentList = if (option == "ALL IP") {
                        fullList.toMutableList()
                    } else {
                        fullList.filter { it.category.equals(option, ignoreCase = true) }
                            .toMutableList()
                    }

                    ipListingAdapter.updateItems(currentList)
                    scrollToTop()
                }
            }
            dropdown.addView(textView)
        }

        // 토글
        val frame = binding.frame4043 as RelativeLayout
        frame.setOnClickListener {
            dropdown.visibility =
                if (dropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }


    private fun setupRecyclerView() {
        ipListingAdapter = IpListingAdapter(emptyList())
        binding.recyclerIpList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ipListingAdapter
        }
    }

    private fun loadInitialData() {
        // API로부터 IP 리스팅 데이터 가져오기
        val ipListingRepository = com.stip.stip.api.repository.IpListingRepository()
        
        // 로딩 중 표시
        fullList = emptyList<IpListingItem>().toMutableList()
        currentList = emptyList<IpListingItem>().toMutableList()
        ipListingAdapter.updateItems(currentList)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // API에서 데이터 가져오기 시도
                val apiItems = ipListingRepository.getIpListing()
                
                if (apiItems.isNotEmpty()) {
                    // API에서 가져온 데이터 사용
                    fullList = apiItems.toMutableList()
                    currentList = fullList.toMutableList()
                    
                    // TradingDataHolder에 데이터 저장하여 앱 전체에서 일관되게 사용
                    // 메모리에 명시된 대로 모든 IP 관련 데이터는 TradingDataHolder.ipListingItems로 통합 관리
                    TradingDataHolder.ipListingItems = fullList
                    
                    // 어댑터 업데이트
                    ipListingAdapter.updateItems(currentList)
                } else {
                    // API 응답이 비어있을 경우 빈 상태의 UI만 표시
                    loadEmptyState()
                }
            } catch (e: Exception) {
                // API 호출 실패 시 빈 상태의 UI만 표시
                android.util.Log.e("IpHomeFragment", "API 데이터 로드 실패: ${e.message}")
                loadEmptyState()
            }
        }
    }
    
    // ...
    /**
     * API 호출 실패 시 빈 UI 상태만 표시
     * 더미 데이터는 사용하지 않고 실제 API 데이터만 사용하는 정책 적용
     */
    private fun loadEmptyState() {
        // 빈 리스트 생성
        val emptyList = emptyList<IpListingItem>()
        
        // 빈 데이터 설정
        fullList = emptyList.toMutableList()
        currentList = emptyList.toMutableList()
        TradingDataHolder.ipListingItems = emptyList
        ipListingAdapter.updateItems(currentList)
        
        // 사용자에게 데이터가 없음을 알리는 UI 업데이트 추가 가능
        // 예: 빈 상태 메시지 표시
    }
    




    private val categoryOptions = listOf(
        "ALL IP", "Patent", "Trademark", "Franchise",
        "Music", "Art", "Movie", "Drama", "BM", "Dance", "Game", "Comics", "Character"
    )


    private fun startAutoPriceUpdate() {
        priceUpdateRunnable = object : Runnable {
            override fun run() {
                refreshPriceData()
                handler.postDelayed(this, 10000) // 10초마다 실제 API에서 데이터 가져오도록 변경
            }
        }
        handler.post(priceUpdateRunnable)
    }


    /**
     * API에서 실제 가격 데이터를 가져오는 함수
     * 더이상 임의의 가격 변동을 생성하지 않고 실제 API 데이터를 사용
     */
    private fun refreshPriceData() {
        // API를 통해 최신 가격 데이터 가져오기
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repository = com.stip.stip.api.repository.IpListingRepository()
                val updatedData = repository.getIpListing()
                
                if (updatedData.isNotEmpty()) {
                    // 기존 리스트 업데이트
                    fullList.clear()
                    fullList.addAll(updatedData)
                    
                    // 현재 필터링된 리스트 업데이트 (카테고리 필터 유지)
                    updateCurrentListWithFilter()
                    
                    // TradingDataHolder 업데이트
                    TradingDataHolder.ipListingItems = fullList.toList()
                    
                    // 어댑터 업데이트
                    ipListingAdapter.updateItems(currentList)
                }
            } catch (e: Exception) {
                android.util.Log.e("IpHomeFragment", "가격 업데이트 실패: ${e.message}")
            }
        }
    }

    /**
     * 외부에서 호출 가능한 IP 리스팅 데이터 갱신 함수
     * 외부에서 TradingDataHolder의 데이터가 업데이트된 후 호출된다
     */
    fun refreshIpListingData() {
        // TradingDataHolder에서 업데이트된 데이터 가져오기
        val updatedData = TradingDataHolder.ipListingItems
        
        if (updatedData.isNotEmpty()) {
            // 기존 리스트 업데이트 - mutableList로 변환하여 사용
            fullList = updatedData.toMutableList()
            
            // 현재 필터링 조건 적용하여 표시 데이터 업데이트
            updateCurrentListWithFilter()
            
            // 어댑터 업데이트 (사용자 UI에 반영)
            activity?.runOnUiThread {
                ipListingAdapter.updateItems(currentList)
            }
        }
    }
    
    /**
     * 현재 필터링 조건에 따라 표시할 리스트 업데이트
     */
    private fun updateCurrentListWithFilter() {
        val selectedCategory = binding.allIp.text.toString()
        val searchQuery = binding.ipSearch.text.toString().trim()
        
        currentList = if (selectedCategory == "ALL IP") {
            fullList.toMutableList()
        } else {
            fullList.filter { it.category == selectedCategory }.toMutableList()
        }
        
        if (searchQuery.isNotEmpty()) {
            currentList = currentList.filter {
                it.ticker.contains(searchQuery, ignoreCase = true) ||
                it.companyName.contains(searchQuery, ignoreCase = true)
            }.toMutableList()
        }
    }

    private fun scrollToTop() {
        binding.recyclerIpList.scrollToPosition(0)
    }


    private fun setupSortListeners() {
        val active = ContextCompat.getColor(requireContext(), R.color.color_main_point)
        val inactive = ContextCompat.getColor(requireContext(), R.color.sort_inactive)

        var isPriceAsc = false
        var isChangeAsc = false
        var isVolumeAsc = false

        fun resetAllSortIcons() {
            binding.icPrimeSortUpFill.setColorFilter(inactive)
            binding.icPrimeSortDownFill.setColorFilter(inactive)
            binding.icChangeSortUpFill.setColorFilter(inactive)
            binding.icChangeSortDownFill.setColorFilter(inactive)
            binding.icVolumeSortUpFill.setColorFilter(inactive)
            binding.icVolumeSortDownFill.setColorFilter(inactive)
        }

        fun sortByPrice() {
            currentList = if (isPriceAsc) {
                currentList.sortedByDescending {
                    it.currentPrice.replace(",", "").toFloatOrNull() ?: 0f
                }.toMutableList()
            } else {
                currentList.sortedBy {
                    it.currentPrice.replace(",", "").toFloatOrNull() ?: 0f
                }.toMutableList()
            }

            resetAllSortIcons()

            if (isPriceAsc) binding.icPrimeSortDownFill.setColorFilter(active)
            else binding.icPrimeSortUpFill.setColorFilter(active)

            // ✅ 티커 화살표 비활성화
            resetTickerSortIcon()

            ipListingAdapter.updateItems(currentList)
            isPriceAsc = !isPriceAsc
        }



        fun sortByChange() {
            currentList = if (isChangeAsc) {
                currentList.sortedByDescending {
                    it.changePercent.replace("%", "").toFloatOrNull() ?: 0f
                }.toMutableList()
            } else {
                currentList.sortedBy { it.changePercent.replace("%", "").toFloatOrNull() ?: 0f }
                    .toMutableList()
            }
            resetAllSortIcons()
            if (isChangeAsc) binding.icChangeSortDownFill.setColorFilter(active)
            else binding.icChangeSortUpFill.setColorFilter(active)
            ipListingAdapter.updateItems(currentList)
            isChangeAsc = !isChangeAsc
        }

        fun sortByVolume() {
            currentList = if (isVolumeAsc) {
                currentList.sortedByDescending {
                    it.volume.replace(",", "")
                        .replace("USD", "")
                        .replace("$", "") // 추가
                        .trim().toFloatOrNull() ?: 0f
                }.toMutableList()
            } else {
                currentList.sortedBy {
                    it.volume.replace(",", "")
                        .replace("USD", "")
                        .replace("$", "") // 추가
                        .trim().toFloatOrNull() ?: 0f
                }.toMutableList()
            }

            resetAllSortIcons()
            if (isVolumeAsc) binding.icVolumeSortDownFill.setColorFilter(active)
            else binding.icVolumeSortUpFill.setColorFilter(active)
            ipListingAdapter.updateItems(currentList)
            isVolumeAsc = !isVolumeAsc
        }


        binding.sortPriceContainer.setOnClickListener { sortByPrice() }
        binding.sortChangeContainer.setOnClickListener { sortByChange() }
        binding.sortVolumeContainer.setOnClickListener { sortByVolume() }

        binding.icPrimeSortUpFill.setOnClickListener { sortByPrice() }
        binding.icPrimeSortDownFill.setOnClickListener { sortByPrice() }
        binding.icChangeSortUpFill.setOnClickListener { sortByChange() }
        binding.icChangeSortDownFill.setOnClickListener { sortByChange() }
        binding.icVolumeSortUpFill.setOnClickListener { sortByVolume() }
        binding.icVolumeSortDownFill.setOnClickListener { sortByVolume() }

        resetAllSortIcons()
    }

    private fun setupSearchListener() {
        binding.ipSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterListByQuery(s.toString())
                scrollToTop()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterListByQuery(query: String) {
        currentList = if (query.isBlank()) {
            fullList.toMutableList()
        } else {
            fullList.filter {
                it.ticker.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        ipListingAdapter.updateItems(currentList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(priceUpdateRunnable)
        _binding = null
    }

    companion object {
        fun newInstance(): IpHomeFragment = IpHomeFragment()
    }
}
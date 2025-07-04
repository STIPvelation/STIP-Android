package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.iphome.adapter.QuotesAdapter
import com.stip.stip.databinding.FragmentIpHomeQuotesBinding
import com.stip.stip.iphome.model.QuoteTick
import com.stip.stip.iphome.model.PriceChangeStatus
import com.stip.stip.api.repository.IpListingRepository
import com.stip.stip.iptransaction.api.IpTransactionService
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class IpHomeQuotesFragment : Fragment() {

    private var _binding: FragmentIpHomeQuotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var quotesAdapter: QuotesAdapter
    private var currentTicker: String? = null
    private var selectedTabId: Int = R.id.button_time // 기본값 시간 탭
    
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L // 5초마다 업데이트
    private val timeQuotesList = mutableListOf<QuoteTick>()
    private var lastPrice: Double = 0.0

    companion object {
        private const val ARG_TICKER = "ticker"
        private const val TAG = "IpHomeQuotesFragment"
        
        fun newInstance(ticker: String?): IpHomeQuotesFragment =
            IpHomeQuotesFragment().apply {
                arguments = Bundle().apply { putString(ARG_TICKER, ticker) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedTabId = savedInstanceState?.getInt("SELECTED_TAB_ID") ?: R.id.button_time
        arguments?.let {
            currentTicker = it.getString(ARG_TICKER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpHomeQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        updateHeadersForTab(selectedTabId == R.id.button_time)
        if (selectedTabId == R.id.button_time) {
            loadTimeQuotes()
        }
        startDataUpdates()
    }

    private fun setupRecyclerView() {
        quotesAdapter = QuotesAdapter(requireContext())
        binding.quotesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesRecyclerView.adapter = quotesAdapter
    }

    private fun setupTabs() {
        binding.toggleButtonGroupTimeDaily.check(selectedTabId)

        binding.toggleButtonGroupTimeDaily.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedTabId = checkedId
                when (checkedId) {
                    R.id.button_time -> {
                        binding.quotesRecyclerView.adapter = quotesAdapter
                        updateHeadersForTab(true)
                        loadTimeQuotes()
                    }
                    R.id.button_daily -> {
                        updateHeadersForTab(false)
                        binding.quotesRecyclerView.adapter = null
                        binding.quotesRecyclerView.isVisible = false
                    }
                }
            }
        }
    }

    fun updateTicker(ticker: String?) {
        currentTicker = ticker
        timeQuotesList.clear()
        if (selectedTabId == R.id.button_time) {
            loadTimeQuotes()
        }
    }

    private fun startDataUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAdded && selectedTabId == R.id.button_time) {
                    loadTimeQuotes()
                }
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun loadTimeQuotes() {
        showLoading(true)
        Log.d(TAG, "시간별 시세 로드 시작 - currentTicker: $currentTicker")
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 먼저 pairId 가져오기
                val repository = IpListingRepository()
                val pairId = withContext(Dispatchers.IO) {
                    repository.getPairIdForTicker(currentTicker)
                }
                Log.d(TAG, "pairId 조회 결과: $pairId")

                if (pairId == null) {
                    Log.e(TAG, "pairId를 찾을 수 없음 - currentTicker: $currentTicker")
                    showError(getString(R.string.error_no_matching_data))
                    return@launch
                }

                // trades/tickers API 호출
                IpTransactionService.getTickers { response, error ->
                    if (error != null) {
                        Log.e(TAG, "실시간 시세 데이터 로드 실패: ${error.message}")
                        showError(getString(R.string.error_loading_data))
                        return@getTickers
                    }

                    response?.let { tickerResponse ->
                        Log.d(TAG, "티커 응답 - success: ${tickerResponse.success}, dataSize: ${tickerResponse.data.size}")
                        
                        if (!tickerResponse.success || tickerResponse.data.isEmpty()) {
                            showError(getString(R.string.error_no_data))
                            return@getTickers
                        }

                        // 현재 ticker에 해당하는 데이터 찾기
                        val tickerData = tickerResponse.data.find { it.pairId == pairId }
                        Log.d(TAG, "찾은 티커 데이터: ${tickerData?.lastPrice ?: "없음"}")

                        if (tickerData == null) {
                            showError(getString(R.string.error_no_matching_data))
                            return@getTickers
                        }

                        // 새로운 데이터 추가
                        val currentTime = System.currentTimeMillis()
                        val priceChangeStatus = when {
                            tickerData.lastPrice > lastPrice -> PriceChangeStatus.UP
                            tickerData.lastPrice < lastPrice -> PriceChangeStatus.DOWN
                            else -> PriceChangeStatus.SAME
                        }

                        val newQuote = QuoteTick(
                            id = UUID.randomUUID().toString(),
                            time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentTime)),
                            price = tickerData.lastPrice,
                            volume = tickerData.volume,
                            priceChangeStatus = priceChangeStatus
                        )

                        timeQuotesList.add(0, newQuote)
                        if (timeQuotesList.size > 30) {
                            timeQuotesList.removeAt(timeQuotesList.size - 1)
                        }

                        lastPrice = tickerData.lastPrice
                        Log.d(TAG, "시간별 시세 업데이트 - 가격: ${tickerData.lastPrice}, 상태: $priceChangeStatus")
                        
                        activity?.runOnUiThread {
                            quotesAdapter.submitList(timeQuotesList.toList())
                            showData()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "시간별 시세 데이터 로드 실패: ${e.message}")
                showError(getString(R.string.error_loading_data))
            }
        }
    }

    private fun updateHeadersForTab(isTimeTab: Boolean) {
        if (isTimeTab) {
            binding.headerTime.text = getString(R.string.quote_header_time)
            binding.headerPrice.text = getString(R.string.quote_header_price_usd)
            binding.headerChange.visibility = View.GONE
            binding.headerVolume.text = getString(R.string.quote_header_executed)
        } else {
            binding.headerTime.text = getString(R.string.quote_header_date)
            binding.headerPrice.text = getString(R.string.quote_header_close)
            binding.headerChange.visibility = View.VISIBLE
            binding.headerVolume.text = getString(R.string.quote_header_volume_with_ticker, currentTicker ?: "티커")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.quotesRecyclerView.isVisible = !isLoading && selectedTabId == R.id.button_time
        binding.errorTextView.isVisible = false
    }

    private fun showData() {
        showLoading(false)
        binding.errorTextView.isVisible = false
        binding.quotesRecyclerView.isVisible = selectedTabId == R.id.button_time
    }

    private fun showError(message: String?) {
        showLoading(false)
        binding.quotesRecyclerView.isVisible = false
        binding.errorTextView.text = message ?: getString(R.string.error_loading_data)
        binding.errorTextView.isVisible = selectedTabId == R.id.button_time
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_TAB_ID", selectedTabId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
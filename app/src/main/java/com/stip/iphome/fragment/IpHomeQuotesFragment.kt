package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.iphome.adapter.DailyQuotesAdapter
import com.stip.stip.R
import com.stip.stip.iphome.adapter.QuotesAdapter
import com.stip.stip.databinding.FragmentIpHomeQuotesBinding
import com.stip.stip.iphome.TradingDataHolder
import kotlinx.coroutines.launch

class IpHomeQuotesFragment : Fragment() {

    private var _binding: FragmentIpHomeQuotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var quotesAdapter: QuotesAdapter
    private lateinit var dailyQuotesAdapter: DailyQuotesAdapter
    private var currentTicker: String? = null
    private var selectedTabId: Int = R.id.button_time // 기본값 시간 탭


    companion object {
        private const val ARG_TICKER = "ticker"
        fun newInstance(ticker: String?): IpHomeQuotesFragment =
            IpHomeQuotesFragment().apply {
                arguments = Bundle().apply { putString(ARG_TICKER, ticker) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 여기 추가
        selectedTabId = savedInstanceState?.getInt("SELECTED_TAB_ID") ?: R.id.button_time

        arguments?.let {
            currentTicker = it.getString(ARG_TICKER)
        }
    }

    fun updateTicker(ticker: String?) {
        currentTicker = ticker
        if (selectedTabId == R.id.button_time) {
            loadTimeQuotes()
            updateHeadersForTab(true, ticker)
        } else {
            loadDailyQuotes()
            updateHeadersForTab(false, ticker)
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
        updateHeadersForTab(isTimeTab = true, currentTicker)
        loadTimeQuotes()
    }




    private fun setupRecyclerView() {
        quotesAdapter = QuotesAdapter(requireContext())
        binding.quotesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.quotesRecyclerView.adapter = quotesAdapter
    }



    private fun setupTabs() {
        binding.toggleButtonGroupTimeDaily.check(selectedTabId)

        // 초기에 어댑터 및 데이터 설정
        if (selectedTabId == R.id.button_time) {
            binding.quotesRecyclerView.adapter = quotesAdapter
            updateHeadersForTab(true, currentTicker)
            loadTimeQuotes()
        } else {
            if (!::dailyQuotesAdapter.isInitialized) {
                dailyQuotesAdapter = DailyQuotesAdapter(requireContext())
            }
            binding.quotesRecyclerView.adapter = dailyQuotesAdapter
            updateHeadersForTab(false, currentTicker)
            loadDailyQuotes()
        }

        binding.toggleButtonGroupTimeDaily.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedTabId = checkedId // ✅ 탭 선택 상태 저장

                when (checkedId) {
                    R.id.button_time -> {
                        binding.quotesRecyclerView.adapter = quotesAdapter
                        updateHeadersForTab(true, currentTicker)
                        loadTimeQuotes()
                    }
                    R.id.button_daily -> {
                        if (!::dailyQuotesAdapter.isInitialized) {
                            dailyQuotesAdapter = DailyQuotesAdapter(requireContext())
                        }
                        binding.quotesRecyclerView.adapter = dailyQuotesAdapter
                        updateHeadersForTab(false, currentTicker)
                        loadDailyQuotes()
                    }
                }
            }
        }
    }





    private fun loadTimeQuotes() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ticker = currentTicker ?: return@launch
                val repository = com.stip.stip.api.repository.QuoteRepository()
                val quotes = repository.getTimeQuotes(ticker, 30)
                
                if (quotes.isNotEmpty()) {
                    // API에서 가져온 데이터 사용
                    quotesAdapter.submitList(quotes)
                } else {
                    // API 응답이 비어있을 경우 빈 리스트 표시
                    quotesAdapter.submitList(emptyList())
                }
            } catch (e: Exception) {
                // API 호출 실패 시 빈 리스트 표시
                android.util.Log.e("IpHomeQuotesFragment", "Time Quotes API 데이터 로드 실패: ${e.message}")
                quotesAdapter.submitList(emptyList())
            } finally {
                showData()
            }
        }
    }

    private fun loadDailyQuotes() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ticker = currentTicker ?: return@launch
                val repository = com.stip.stip.api.repository.QuoteRepository()
                val quotes = repository.getDailyQuotes(ticker, 20)
                
                if (quotes.isNotEmpty()) {
                    // API에서 가져온 데이터 사용
                    dailyQuotesAdapter.submitList(quotes)
                } else {
                    // API 응답이 비어있을 경우 빈 리스트 표시
                    dailyQuotesAdapter.submitList(emptyList())
                }
            } catch (e: Exception) {
                // API 호출 실패 시 빈 리스트 표시
                android.util.Log.e("IpHomeQuotesFragment", "Daily Quotes API 데이터 로드 실패: ${e.message}")
                dailyQuotesAdapter.submitList(emptyList())
            } finally {
                showData()
            }
        }
    }

    // 더미 데이터 생성 함수 삭제 - API를 통해 실제 데이터를 가져오도록 변경

    // 일별 호가 더미 데이터 생성 함수 삭제 - API를 통해 실제 데이터를 가져오도록 변경



    private fun updateHeadersForTab(isTimeTab: Boolean, ticker: String?) {
        if (isTimeTab) {
            binding.headerTime.text = getString(R.string.quote_header_time)
            binding.headerPrice.text = getString(R.string.quote_header_price_usd)
            binding.headerChange.visibility = View.GONE
            binding.headerVolume.text = getString(R.string.quote_header_executed)
        } else {
            binding.headerTime.text = getString(R.string.quote_header_date)
            binding.headerPrice.text = getString(R.string.quote_header_close)
            binding.headerChange.visibility = View.VISIBLE
            binding.headerVolume.text = getString(R.string.quote_header_volume_with_ticker, ticker ?: "티커")
        }
    }

    private fun getCurrentPriceFromIpListing(ticker: String?): Double {
        return TradingDataHolder.ipListingItems.firstOrNull {
            it.ticker.equals(ticker, ignoreCase = true)
        }?.currentPrice?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.quotesRecyclerView.isVisible = !isLoading
        binding.errorTextView.isVisible = false
    }

    private fun showData() {
        showLoading(false)
        binding.errorTextView.isVisible = false
        binding.quotesRecyclerView.isVisible = true
    }

    private fun showError(message: String?) {
        showLoading(false)
        binding.quotesRecyclerView.isVisible = false
        binding.errorTextView.text = message ?: getString(R.string.error_loading_data)
        binding.errorTextView.isVisible = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_TAB_ID", selectedTabId) // 현재 탭 상태 저장
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
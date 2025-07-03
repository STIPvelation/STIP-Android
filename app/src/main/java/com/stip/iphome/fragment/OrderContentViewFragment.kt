package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.model.OrderBookItem
import com.stip.stip.order.adapter.OrderBookAdapter
import com.stip.stip.order.book.OrderBookManager
import com.stip.stip.order.coordinator.OrderDataCoordinator
import com.stip.stip.iphome.TradingDataHolder
import java.text.DecimalFormat

interface OnOrderBookItemClickListener {
    fun onPriceClicked(price: String)
}

class OrderContentViewFragment : Fragment(), OnOrderBookItemClickListener {
    private var _binding: FragmentOrderContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderDataCoordinator: OrderDataCoordinator
    private var orderBookAdapter: OrderBookAdapter? = null
    private var orderBookManager: OrderBookManager? = null
    private var initialTicker: String? = null

    companion object {
        private const val TAG = "OrderContentViewFragment"
        private const val ARG_TICKER = "ticker"
        fun newInstance(ticker: String?): OrderContentViewFragment {
            return OrderContentViewFragment().apply {
                arguments = Bundle().apply { putString(ARG_TICKER, ticker) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { initialTicker = it.getString(ARG_TICKER) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize Core Logic
            orderDataCoordinator = OrderDataCoordinator(initialTicker)

            // Setup OrderBook
            setupOrderBook()
            setupOrderBookManager()

            // Start OrderBook updates
            orderBookManager?.let { manager ->
                manager.initializeAndStart()
                manager.startAutoUpdate()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OrderContentViewFragment", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            orderBookManager?.startAutoUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            orderBookManager?.stopAutoUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    private fun setupOrderBook() {
        try {
            orderBookAdapter = OrderBookAdapter(orderDataCoordinator.currentPrice, this)
            binding.recyclerOrderBook.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = orderBookAdapter
                setHasFixedSize(true)
                itemAnimator = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupOrderBook", e)
        }
    }

    private fun setupOrderBookManager() {
        try {
            val numberParseFormat = DecimalFormat("#,##0.00")
            val fixedTwoDecimalFormatter = DecimalFormat("#,##0.00")

            orderBookAdapter?.let { adapter ->
                orderBookManager = OrderBookManager(
                    context = requireContext(),
                    recyclerView = binding.recyclerOrderBook,
                    orderBookAdapter = adapter,
                    numberParseFormat = numberParseFormat,
                    fixedTwoDecimalFormatter = fixedTwoDecimalFormatter,
                    getCurrentPrice = { orderDataCoordinator.currentPrice },
                    binding = binding,
                    getCurrentPairId = { 
                        TradingDataHolder.ipListingItems
                            .find { it.ticker == orderDataCoordinator.currentTicker }?.registrationNumber 
                    }
                ).also { manager ->
                    manager.setupBottomOptionListeners()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupOrderBookManager", e)
        }
    }

    fun updateTicker(ticker: String?) {
        Log.d(TAG, "updateTicker called with: $ticker")
        if (_binding == null || !isAdded) {
            Log.w(TAG, "updateTicker called but Fragment view is not available.")
            initialTicker = ticker
            return
        }

        try {
            orderDataCoordinator.updateTicker(ticker)
            val currentPrice = orderDataCoordinator.currentPrice
            Log.d(TAG, "🔄 Updating OrderBook for ticker: $ticker, price: $currentPrice")
            orderBookManager?.let { manager ->
                manager.updateCurrentPrice(currentPrice)
                manager.initializeAndStart()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTicker", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            orderBookManager?.release()
            binding.recyclerOrderBook.adapter = null
            orderBookManager = null
            orderBookAdapter = null
            _binding = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView", e)
        }
    }

    override fun onPriceClicked(price: String) {
        // 호가 아이템 클릭 처리
        try {
            Log.d(TAG, "Price clicked: $price")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPriceClicked", e)
        }
    }
} 
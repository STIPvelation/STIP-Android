package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.order.coordinator.OrderDataCoordinator
import com.stip.stip.order.OrderUIInitializer
import com.stip.stip.order.OrderUIStateManager
import com.stip.stip.order.OrderHistoryManager
import com.stip.stip.order.adapter.OrderBookAdapter
import com.stip.stip.order.book.OrderBookManager
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.iphome.adapter.UnfilledOrderAdapter
import com.stip.stip.iphome.util.OrderUtils
import com.stip.stip.order.adapter.FilledOrderAdapter
import com.stip.stip.order.button.OrderButtonHandler
import com.stip.stip.order.OrderValidator
import com.stip.stip.signup.utils.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.DecimalFormat

interface OnOrderBookItemClickListener {
    fun onPriceClicked(price: String)
}

class OrderContentViewFragment : Fragment(), OnOrderBookItemClickListener {
    private var _binding: FragmentOrderContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderDataCoordinator: OrderDataCoordinator
    private lateinit var uiInitializer: OrderUIInitializer
    private lateinit var uiStateManager: OrderUIStateManager
    private lateinit var historyManager: OrderHistoryManager
    private lateinit var orderBookAdapter: OrderBookAdapter
    private lateinit var orderBookManager: OrderBookManager
    private lateinit var filledOrderAdapter: FilledOrderAdapter
    private lateinit var unfilledOrderAdapter: UnfilledOrderAdapter
    private lateinit var validator: OrderValidator
    private lateinit var orderButtonHandler: OrderButtonHandler
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
        // PreferenceUtil 초기화
        PreferenceUtil.init(requireContext())
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

            // Initialize Adapters
            filledOrderAdapter = FilledOrderAdapter()
            unfilledOrderAdapter = UnfilledOrderAdapter()

            // Initialize UI Components
            uiInitializer = OrderUIInitializer(requireContext(), binding)
            historyManager = OrderHistoryManager(
                context = requireContext(),
                binding = binding,
                unfilledAdapter = unfilledOrderAdapter,
                filledAdapter = filledOrderAdapter,
                fragmentManager = parentFragmentManager,
                coroutineScope = CoroutineScope(Dispatchers.Main)
            )

            // Setup adapter callbacks
            unfilledOrderAdapter.onSelectionChanged = { hasSelection ->
                historyManager.updateCancelButtonState(hasSelection)
            }

            validator = OrderValidator(
                context = requireContext(),
                binding = binding,
                getCurrentPrice = { orderDataCoordinator.currentPrice },
                getCurrentTicker = { orderDataCoordinator.currentTicker },
                availableUsdBalance = { orderDataCoordinator.availableUsdBalance },
                heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
                feeRate = 0.001,
                minimumOrderValue = 10.0,
                numberParseFormat = DecimalFormat("#,##0.00"),
                fixedTwoDecimalFormatter = DecimalFormat("#,##0.00"),
                showToast = { msg -> OrderUtils.showToast(requireContext(), msg) },
                showErrorDialog = { titleRes, message, colorRes -> OrderUtils.showErrorDialog(parentFragmentManager, titleRes, message, colorRes) }
            )

            orderButtonHandler = OrderButtonHandler(
                context = requireContext(),
                binding = binding,
                validator = validator,
                numberParseFormat = DecimalFormat("#,##0.00"),
                fixedTwoDecimalFormatter = DecimalFormat("#,##0.00"),
                getCurrentPrice = { orderDataCoordinator.currentPrice },
                getFeeRate = { 0.001 },
                currentTicker = { orderDataCoordinator.currentTicker },
                minimumOrderValue = 10.0,
                availableUsdBalance = { orderDataCoordinator.availableUsdBalance },
                heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
                showToast = { msg -> OrderUtils.showToast(requireContext(), msg) },
                showErrorDialog = { titleRes, message, colorRes -> OrderUtils.showErrorDialog(parentFragmentManager, titleRes, message, colorRes) },
                parentFragmentManager = parentFragmentManager,
                getCurrentPairId = { 
                    TradingDataHolder.ipListingItems
                        .find { it.ticker == orderDataCoordinator.currentTicker }?.registrationNumber 
                }
            )

            uiStateManager = OrderUIStateManager(
                requireContext(),
                binding,
                orderDataCoordinator,
                historyManager,
                uiInitializer
            )

            // Setup UI Components
            uiInitializer.setupTabLayoutColors { position -> uiStateManager.handleTabSelection(position) }

            // Setup OrderBook
            setupOrderBook()
            setupOrderBookManager()

            // Start OrderBook updates
            orderBookManager.initializeAndStart()
            orderBookManager.startAutoUpdate()

            // Initialize UI State
            val initialTabPosition = binding.tabLayoutOrderMode.selectedTabPosition
            
            // 매수 탭으로 초기화
            if (initialTabPosition != 0) {
                binding.tabLayoutOrderMode.selectTab(binding.tabLayoutOrderMode.getTabAt(0))
            }
            
            // UI 상태 초기화
            uiStateManager.handleTabSelection(0, true)
            
            // 내역 탭이 아닌 경우 숨김
            historyManager.hide()

            // 버튼 핸들러 초기화
            orderButtonHandler.setupOrderButtonClickListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OrderContentViewFragment", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            orderBookManager.startAutoUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            orderBookManager.stopAutoUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    private fun setupOrderBook() {
        try {
            orderBookAdapter = OrderBookAdapter(orderDataCoordinator.currentPrice, this)
            binding.recyclerOrderBook.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
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

            orderBookManager = OrderBookManager(
                context = requireContext(),
                recyclerView = binding.recyclerOrderBook,
                orderBookAdapter = orderBookAdapter,
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
            orderBookManager.updateCurrentPrice(currentPrice)
            orderBookManager.initializeAndStart()
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTicker", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            orderBookManager.release()
            binding.recyclerOrderBook.adapter = null
            binding.recyclerViewUnfilledOrders.adapter = null
            binding.recyclerViewFilledOrders.adapter = null
            _binding = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView", e)
        }
    }

    override fun onPriceClicked(price: String) {
        try {
            Log.d(TAG, "Price clicked: $price")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPriceClicked", e)
        }
    }
} 
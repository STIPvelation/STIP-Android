package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // ❗️ Toast import 추가됨
import androidx.fragment.app.Fragment
import com.stip.stip.FilledOrderAdapter
import com.stip.stip.OrderBookAdapter
import com.stip.stip.R
import com.stip.stip.iphome.adapter.UnfilledOrderAdapter
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.order.*
import com.stip.stip.iphome.util.OrderUtils
import com.stip.stip.order.OrderDataCoordinator
import com.stip.stip.order.OrderHistoryManager
import com.stip.stip.order.OrderUIStateManager
import com.stip.stip.MainActivity


interface OnOrderBookItemClickListener {
    fun onPriceClicked(price: String)
}

class OrderContentViewFragment : Fragment(), OnOrderBookItemClickListener {

    private var _binding: FragmentOrderContentBinding? = null
    private val binding get() = _binding!!

    // Managers and Handlers
    private lateinit var orderDataCoordinator: OrderDataCoordinator
    private lateinit var orderButtonHandler: OrderButtonHandler
    private lateinit var uiInitializer: OrderUIInitializer
    private lateinit var inputHandler: OrderInputHandler
    private lateinit var bookManager: OrderBookManager
    private lateinit var validator: OrderValidator
    private lateinit var historyManager: OrderHistoryManager
    private lateinit var orderTickerManager: OrderTickerManager
    private lateinit var orderConfirmResultHandler: OrderConfirmResultHandler
    private lateinit var orderInfoManager: OrderInfoManager
    private lateinit var uiStateManager: OrderUIStateManager



    // Adapters
    private var initialTicker: String? = null
    private lateinit var filledOrderAdapter: FilledOrderAdapter
    private lateinit var unfilledOrderAdapter: UnfilledOrderAdapter // ✅ 선언 확인

    private lateinit var orderEntryViews: List<View>

    companion object {
        private const val TAG = "OrderContentViewFragment" // ✅ 로그 태그 정의됨
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialBalance = 10000.0
        val initialHoldings = 200.50
        val feeRate = 0.001
        val minimumOrderValue = 10.0

        // --- Initialize Adapters ---
        filledOrderAdapter = FilledOrderAdapter()
        unfilledOrderAdapter = UnfilledOrderAdapter() // ✅ 초기화 확인

        // --- Initialize Core Logic ---
        orderDataCoordinator = OrderDataCoordinator(initialTicker, initialBalance, initialHoldings, feeRate)

        // --- Initialize Managers & Handlers (Dependencies in Order) ---
        inputHandler = OrderInputHandler(
            requireContext(), binding, OrderUtils.numberParseFormat, OrderUtils.fixedTwoDecimalFormatter,
            getCurrentPrice = { orderDataCoordinator.currentPrice.toDouble() },
            getFeeRate = { feeRate },
            availableUsdBalance = { orderDataCoordinator.availableUsdBalance },
            heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
            getCurrentTicker = { orderDataCoordinator.currentTicker },
            getCurrentOrderType = { binding.radioGroupOrderType.checkedRadioButtonId }
        )

        uiInitializer = OrderUIInitializer(requireContext(), binding)

        val orderBookAdapterInstance = OrderBookAdapter(orderDataCoordinator.currentPrice, this)
        bookManager = OrderBookManager(
            requireContext(), binding.recyclerOrderBook, orderBookAdapterInstance,
            OrderUtils.numberParseFormat, OrderUtils.fixedTwoDecimalFormatter,
            getCurrentPrice = { orderDataCoordinator.currentPrice },
            binding = binding
        )

        validator = OrderValidator(
            requireContext(), binding,
            getCurrentPrice = { orderDataCoordinator.currentPrice },
            getCurrentTicker = { orderDataCoordinator.currentTicker },
            availableUsdBalance = { orderDataCoordinator.availableUsdBalance },
            heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
            feeRate = feeRate, minimumOrderValue = minimumOrderValue,
            numberParseFormat = OrderUtils.numberParseFormat, fixedTwoDecimalFormatter = OrderUtils.fixedTwoDecimalFormatter,
            showToast = { msg -> OrderUtils.showToast(requireContext(), msg) },
            showErrorDialog = { titleRes, message, colorRes -> OrderUtils.showErrorDialog(parentFragmentManager, titleRes, message, colorRes) }
        )

        // ✅ OrderHistoryManager 생성 시 parentFragmentManager 전달 확인됨
        historyManager = OrderHistoryManager(
            requireContext(), binding, unfilledOrderAdapter, filledOrderAdapter,
            parentFragmentManager // ✅ FragmentManager 전달
        )
        // ✅ 어댑터 콜백 설정 확인됨 (historyManager 생성 후)
        unfilledOrderAdapter.onSelectionChanged = { hasSelection ->
            historyManager.updateCancelButtonState(hasSelection)
        }

        orderInfoManager = OrderInfoManager(
            context = requireContext(), binding = binding,
            getCurrentPrice = { orderDataCoordinator.currentPrice },
            getHeldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
            getAverageBuyPrice = { orderDataCoordinator.getAverageBuyPrice() },
            getCurrentTicker = { orderDataCoordinator.currentTicker },
            userHoldsAsset = { orderDataCoordinator.userHoldsAsset }
        )

        uiStateManager = OrderUIStateManager(
            requireContext(), binding, orderDataCoordinator, historyManager, uiInitializer
        )

        // uiInitializer 설정은 의존하는 uiStateManager 초기화 후
        uiInitializer.setupTabLayoutColors { position -> uiStateManager.handleTabSelection(position) }
        uiInitializer.setupRadioGroupListener(
            currentTicker = orderDataCoordinator.currentTicker,
            resetOrderInputsToZero = { inputHandler.resetInputs() }
        )

        orderTickerManager = OrderTickerManager(
            orderDataCoordinator,
            { // onTickerChanged
                orderInfoManager.updateTradingInfoViewContent()
                uiStateManager.updateTradingInfoVisibility()
                uiStateManager.updateOrderAvailableDisplay()
                bookManager.initializeAndStart()
            },
            { // onPriceChanged
                bookManager.updateCurrentPrice(orderDataCoordinator.currentPrice)
                orderInfoManager.updateTradingInfoViewContent()
            }
        )

        orderButtonHandler = OrderButtonHandler(
            context = requireContext(), binding = binding,
            numberParseFormat = OrderUtils.numberParseFormat, fixedTwoDecimalFormatter = OrderUtils.fixedTwoDecimalFormatter,
            getCurrentPrice = { orderDataCoordinator.currentPrice }, getFeeRate = { feeRate },
            currentTicker = { orderDataCoordinator.currentTicker }, minimumOrderValue = minimumOrderValue,
            availableUsdBalance = { orderDataCoordinator.availableUsdBalance }, heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
            showToast = { msg -> OrderUtils.showToast(requireContext(), msg) },
            showErrorDialog = { title, message, colorRes -> OrderUtils.showErrorDialog(parentFragmentManager, title, message, colorRes) },
            parentFragmentManager = parentFragmentManager, validator = validator
        )

        orderConfirmResultHandler = OrderConfirmResultHandler(
            this@OrderContentViewFragment, binding,
        ) { isBuy: Boolean, quantity: Double, price: Double? ->
            // Order Success Callback
            orderDataCoordinator.adjustHoldingsAfterOrder(isBuy, quantity, price)
            inputHandler.resetInputs()
            orderInfoManager.updateTradingInfoViewContent()
            uiStateManager.updateOrderAvailableDisplay()
            uiStateManager.updateTradingInfoVisibility()
            historyManager.loadFilledOrdersIfNeeded()

            // Show Success Dialog
            val titleRes = if (isBuy) R.string.dialog_title_buy_order_submitted else R.string.dialog_title_sell_order_submitted
            val messageRes = if (isBuy) R.string.order_success_message_buy else R.string.order_success_message_sell
            val message = getString(messageRes)
            val titleColorRes = if (isBuy) R.color.percentage_positive_red else R.color.percentage_negative_blue
            OrderUtils.showErrorDialog(parentFragmentManager, titleRes, message, titleColorRes)
        }

        // --- Setup Listeners and Initial State ---
        initializeOrderEntryViews()
        inputHandler.setupInputListeners()
        inputHandler.setupPriceAdjustmentButtons()
        inputHandler.setupQuantitySpinner()
        inputHandler.setupResetButton()
        bookManager.setupBottomOptionListeners()
        orderInfoManager.updateTradingInfoViewContent()
        orderButtonHandler.setupOrderButtonClickListeners()
        orderConfirmResultHandler.registerResultListener()
        setupCancelConfirmResultListener() // ✅ 취소 확인 결과 리스너 호출 확인됨
        setupOrderTypeExplanationPopup()
        setupFilledFilterResultListener()

        binding.iconFilter.setOnClickListener {
            FilledFilterDialogFragment.newInstance()
                .show(parentFragmentManager, FilledFilterDialogFragment.TAG)
        }




        // --- Activate Components ---
        bookManager.initializeAndStart()
        historyManager.activate()
        uiStateManager.handleTabSelection(binding.tabLayoutOrderMode.selectedTabPosition, true)
        orderTickerManager.updateTicker(initialTicker)

    } // --- End of onViewCreated ---

    private fun setupFilledFilterResultListener() {
        parentFragmentManager.setFragmentResultListener(
            FilledFilterDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val types = bundle.getStringArrayList("types") ?: emptyList()
            val startDate = bundle.getString("startDate") ?: ""
            val endDate = bundle.getString("endDate") ?: ""

            // ✅ 여기서 실제 필터 적용 로직 호출 가능
            Log.d("FilterResult", "✅ $types / $startDate ~ $endDate")

            // 예: OrderHistoryManager에 적용
            historyManager.applyFilter(types, startDate, endDate)
        }
    }

    // ✅ 추가된 함수: 취소 확인 다이얼로그 결과 처리 리스너 설정
    private fun setupCancelConfirmResultListener() {
        parentFragmentManager.setFragmentResultListener(
            CancelConfirmDialogFragment.REQUEST_KEY, // 다이얼로그에서 정의한 요청 키
            viewLifecycleOwner // Fragment의 LifecycleOwner 사용
        ) { requestKey, bundle ->
            // 결과 처리
            val confirmed = bundle.getBoolean(CancelConfirmDialogFragment.RESULT_KEY_CONFIRMED, false)
            if (confirmed) {
                // --- ▼▼▼ 사용자가 '확인'을 눌렀을 때 실행될 로직 ▼▼▼ ---
                // ✅ unfilledAdapter 접근 확인됨
                val selectedOrderIds = unfilledOrderAdapter.getSelectedOrderIds()
                unfilledOrderAdapter.clearSelection()

                // 1. 토스트 메시지 표시
                Toast.makeText(context, R.string.toast_cancel_request_sent, Toast.LENGTH_SHORT).show()

                // 2. TODO: 실제 주문 취소 API 호출 (ViewModel 또는 Repository 통해 실행 권장)
                Log.w(TAG, "TODO: Implement actual order cancellation API call for IDs: $selectedOrderIds")

                // 3. 어댑터 선택 상태 초기화
                // ✅ unfilledAdapter 접근 확인됨
                unfilledOrderAdapter.clearSelection() // ✅


                // 4. 미체결 주문 목록 새로고침 (ViewModel을 통해 데이터 갱신 권장)
                // ✅ historyManager 접근 확인됨
                historyManager.handleTabClick(true) // 미체결 탭 강제 리로드

                // --- ▲▲▲ 사용자가 '확인'을 눌렀을 때 실행될 로직 ▲▲▲ ---
            } else {
                // 사용자가 '취소'를 눌렀을 때 (필요시 처리)
                Log.d(TAG, "Cancellation cancelled by user.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bookManager.release()
        // 어댑터 null 설정
        _binding?.let {
            it.recyclerViewHistory.adapter = null
            it.recyclerViewUnfilledOrders.adapter = null
            it.recyclerOrderBook.adapter = null
        }
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        bookManager.stopAutoUpdate()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideHeaderAndTabs() // ✅ 헤더 숨기기 복구
    }


    override fun onPriceClicked(price: String) {
        inputHandler.handleOrderBookPriceClick(price)
    }

    private fun setupOrderTypeExplanationPopup() {
        binding.tradingInfoView?.textLabelSettlement?.setOnClickListener {
            // OrderTypeInfoDialogFragment.show(parentFragmentManager) // show 함수 확인 필요
            OrderTypeInfoDialogFragment().show(parentFragmentManager, OrderTypeInfoDialogFragment.TAG) // 표준적인 show 호출
        }
    }

    private fun initializeOrderEntryViews() {
        if (_binding == null) { orderEntryViews = emptyList(); return }
        orderEntryViews = listOfNotNull(
            binding.radioGroupOrderType,
            binding.rowOrderAvailable,
            binding.orderInputContainer,
            binding.buttonContainer,
            binding.tradingInfoView?.root
        )
    }

    private fun navigateToSiblingTicker(offset: Int) {
        // 네비게이션 로직 (변경 없음)
        val list = TradingDataHolder.ipListingItems; if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.ticker == orderDataCoordinator.currentTicker }
        if (currentIdx != -1) {
            val targetIdx = (currentIdx + offset + list.size) % list.size;
            val targetItem = list[targetIdx];
            if (isAdded && activity?.supportFragmentManager != null) {
                val fragment = OrderContentViewFragment.newInstance(targetItem.ticker)
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container, fragment) // R.id.fragment_container ID 확인
                    ?.commitAllowingStateLoss()
            } else {
                Log.w(TAG, "Fragment navigation failed: not attached or FragmentManager unavailable.")
            }
        }
    }

    // 외부에서 호출될 때 사용
    fun updateTicker(ticker: String?) {
        Log.d(TAG, "updateTicker called externally with: $ticker")
        if (_binding == null || !isAdded) {
            Log.w(TAG, "updateTicker called but Fragment view is not available.")
            initialTicker = ticker
            return
        }

        // 1. 데이터 코디네이터 업데이트
        orderDataCoordinator.updateTicker(ticker)

        // 2. 관련 UI 업데이트
        orderInfoManager.updateTradingInfoViewContent()
        uiStateManager.updateOrderAvailableDisplay()
        uiStateManager.updateTradingInfoVisibility()
        inputHandler.resetInputs()

        // 3. 호가창 업데이트
        val currentPrice = orderDataCoordinator.currentPrice
        Log.d(TAG, "🔄 Updating OrderBook for ticker: $ticker, price: $currentPrice")
        bookManager.updateCurrentPrice(currentPrice)
        bookManager.initializeAndStart()

        // 4. 티커 매니저 업데이트
        orderTickerManager.updateTicker(ticker)

        // 5. 내역 탭 새로고침
        if(historyManager.isUnfilledTabSelected) { // ✅ isUnfilledTabSelected 접근 확인됨 (public 가정)
            historyManager.handleTabClick(true)
        } else {
            historyManager.handleTabClick(false)
        }
    }

} // --- End of Fragment ---
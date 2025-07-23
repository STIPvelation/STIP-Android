package com.stip.stip.iphome.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stip.stip.R
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
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.widget.Toast
import com.stip.order.api.OrderService
import com.stip.stip.api.RetrofitClient
import com.stip.stip.iphome.fragment.CancelConfirmDialogFragment
import com.stip.stip.order.OrderInputHandler

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
    private lateinit var orderInputHandler: OrderInputHandler
    private var initialTicker: String? = null
    private val orderService: OrderService = RetrofitClient.createOrderService()

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
            
            // 잔액 업데이트 콜백 설정
            orderDataCoordinator.setOnBalanceUpdated {
                // UI 업데이트를 메인 스레드에서 실행
                requireActivity().runOnUiThread {
                    try {
                        // 주문 가능 금액 업데이트
                        orderInputHandler.updateOrderAvailableDisplay()
                        // 거래 정보 업데이트
                        orderInputHandler.updateTradingInfoContent()
                        // 주문 버튼 상태 업데이트
                        orderButtonHandler.updateOrderButtonStates()
                    } catch (e: Exception) {
                        Log.e(TAG, "잔액 업데이트 중 UI 오류", e)
                    }
                }
            }

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
                coroutineScope = CoroutineScope(Dispatchers.Main),
                orderDataCoordinator = orderDataCoordinator
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

            // OrderInputHandler 초기화
            orderInputHandler = OrderInputHandler(
                context = requireContext(),
                binding = binding,
                numberParseFormat = DecimalFormat("#,##0.00"),
                fixedTwoDecimalFormatter = DecimalFormat("#,##0.00"),
                getCurrentPrice = { orderDataCoordinator.currentPrice.toDouble() },
                getFeeRate = { 0.001 },
                availableUsdBalance = { orderDataCoordinator.availableUsdBalance },
                heldAssetQuantity = { orderDataCoordinator.heldAssetQuantity },
                getCurrentTicker = { orderDataCoordinator.currentTicker },
                getCurrentOrderType = { binding.radioGroupOrderType.checkedRadioButtonId },
                getHeldAssetEvalAmount = { orderDataCoordinator.getActualSellableEvalAmount() }
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
            
            // OrderInputHandler 설정
            orderInputHandler.setupInputListeners()
            orderInputHandler.setupPriceAdjustmentButtons()
            orderInputHandler.setupQuantitySpinner()
            orderInputHandler.setupResetButton()
            
            // 초기 UI 상태 설정
            orderInputHandler.updateUiForOrderTypeChange()
            
            // 주문 유형 라디오 버튼 리스너 설정 (시장 주문 시 가격 필드 비활성화 등)
            uiInitializer.setupRadioGroupListener(
                currentTicker = orderDataCoordinator.currentTicker,
                resetOrderInputsToZero = {
                    // 입력값 초기화 로직
                    binding.editTextLimitPrice.setText("")
                    binding.editTextQuantity.setText("")
                    binding.editTextTriggerPrice?.setText("")
                },
                updateInputHandlerUI = {
                    // OrderInputHandler의 UI 업데이트 메서드 호출
                    orderInputHandler.updateUiForOrderTypeChange()
                }
            )
            binding.tabLayoutOrderMode.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    tab?.let { 
                        // 탭 변경 시 주문 유형을 지정가로 초기화
                        binding.radioGroupOrderType.check(R.id.radio_limit_order)
                        
                        // 입력값 초기화
                        binding.editTextLimitPrice.setText("")
                        binding.editTextQuantity.setText("")
                        binding.editTextTriggerPrice?.setText("")
                        
                        uiStateManager.handleTabSelection(it.position)
                        // 탭 변경 시 OrderInputHandler UI 업데이트
                        orderInputHandler.updateUiForOrderTypeChange()
                        orderInputHandler.updateOrderAvailableDisplay()
                    }
                }
                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            })

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
            
            // 주문 취소 확인 결과 리스너 설정
            setupCancelConfirmResultListener()

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
            // 주문창에 가격 자동 반영
            orderInputHandler.handleOrderBookPriceClick(price)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPriceClicked", e)
        }
    }
    
    /**
     * 주문 취소 확인 다이얼로그 결과 처리 리스너 설정
     */
    private fun setupCancelConfirmResultListener() {
        Log.d(TAG, "취소 확인 다이얼로그 결과 리스너 설정 완료")
        
        parentFragmentManager.setFragmentResultListener(
            CancelConfirmDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { requestKey, bundle ->
            Log.d(TAG, "다이얼로그 결과 수신: requestKey=$requestKey")
            
            val confirmed = bundle.getBoolean(CancelConfirmDialogFragment.RESULT_KEY_CONFIRMED, false)
            Log.d(TAG, "사용자 선택: confirmed=$confirmed")
            
            if (confirmed) {
                Log.d(TAG, "사용자가 주문 취소를 확인했습니다.")
                
                val selectedOrderIds = unfilledOrderAdapter.getSelectedOrderIds()
                Log.d(TAG, "취소할 주문 ID들: $selectedOrderIds")
                
                if (selectedOrderIds.isEmpty()) {
                    Log.w(TAG, "취소할 주문이 없습니다.")
                    Toast.makeText(context, "취소할 주문이 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setFragmentResultListener
                }

                // 1. 삭제 진행 메시지 표시
                Toast.makeText(context, "주문을 삭제하는 중입니다...", Toast.LENGTH_SHORT).show()

                // 2. 어댑터 선택 상태 초기화
                unfilledOrderAdapter.clearSelection()
                Log.d(TAG, "어댑터 선택 상태 초기화 완료")

                // 3. 실제 주문 삭제 API 호출
                Log.d(TAG, "주문 삭제 API 호출 시작: $selectedOrderIds")
                deleteOrdersSequentially(selectedOrderIds)

            } else {
                Log.d(TAG, "사용자가 주문 취소를 거부했습니다.")
            }
        }
    }
    
    /**
     * 주문들을 순차적으로 삭제합니다.
     * @param orderIds 삭제할 주문 ID 리스트
     */
    private fun deleteOrdersSequentially(orderIds: List<String>) {
        if (orderIds.isEmpty()) {
            Log.w(TAG, "삭제할 주문 ID가 없습니다.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var successCount = 0
            var failCount = 0

            for (orderId in orderIds) {
                try {
                    Log.d(TAG, "주문 삭제 API 호출: $orderId")
                    val response = orderService.deleteOrder(orderId)
                    
                    if (response.isSuccessful) {
                        val deleteResponse = response.body()
                        if (deleteResponse?.success == true) {
                            successCount++
                            Log.d(TAG, "주문 삭제 성공: $orderId - ${deleteResponse.message}")
                            
                            // 각 주문이 삭제될 때마다 UI 업데이트
                            CoroutineScope(Dispatchers.Main).launch {
                                refreshUnfilledOrders()
                            }
                        } else {
                            failCount++
                            Log.e(TAG, "주문 삭제 실패: $orderId - ${deleteResponse?.message ?: "알 수 없는 오류"}")
                        }
                    } else {
                        failCount++
                        Log.e(TAG, "주문 삭제 HTTP 오류: $orderId - ${response.code()}: ${response.message()}")
                    }
                } catch (e: Exception) {
                    failCount++
                    Log.e(TAG, "주문 삭제 예외 발생: $orderId", e)
                }
            }

            // UI 스레드에서 결과 처리
            CoroutineScope(Dispatchers.Main).launch {
                val totalCount = orderIds.size
                val resultMessage = if (failCount == 0) {
                    "$totalCount 개의 주문이 성공적으로 삭제되었습니다."
                } else if (successCount == 0) {
                    "모든 주문 삭제에 실패했습니다. ($failCount 개 실패)"
                } else {
                    "$successCount 개 성공, $failCount 개 실패했습니다."
                }

                Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()

                // 성공한 경우가 있으면 주문 목록 새로고침
                if (successCount > 0) {
                    refreshUnfilledOrders() // 미체결 탭 리로드
                }
                
                // 모든 처리가 완료된 후 최종 새로고침
                binding.root.postDelayed({
                    Log.d(TAG, "최종 새로고침 실행")
                    refreshUnfilledOrders()
                }, 1500)
            }
        }
    }
    
    /**
     * 미체결 주문 목록을 강제로 새로고침합니다.
     */
    private fun refreshUnfilledOrders() {
        try {
            Log.d(TAG, "미체결 주문 목록 강제 새로고침 시작")
            
            // 1. 현재 미체결 탭이 선택되어 있는지 확인
            if (historyManager.isUnfilledTabSelected) {
                Log.d(TAG, "미체결 탭이 선택되어 있음 - 강제 새로고침")
                
                // 2. 히스토리 매니저의 강제 새로고침 메서드 호출
                historyManager.forceRefreshUnfilledOrders()
                
                // 3. 약간의 지연 후 다시 한 번 새로고침 (비동기 처리 대응)
                binding.root.postDelayed({
                    Log.d(TAG, "지연된 새로고침 실행")
                    historyManager.forceRefreshUnfilledOrders()
                }, 1000)
                
            } else {
                Log.d(TAG, "미체결 탭이 선택되지 않음 - 탭 전환 후 새로고침")
                // 미체결 탭으로 전환하고 강제 새로고침
                historyManager.handleTabClick(true)
                binding.root.postDelayed({
                    historyManager.forceRefreshUnfilledOrders()
                }, 500)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "미체결 주문 목록 새로고침 중 오류 발생", e)
        }
    }
} 
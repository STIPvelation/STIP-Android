package com.stip.stip.order

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.iptransaction.model.IpInvestmentItem
import com.stip.stip.iptransaction.model.UnfilledOrder
import com.stip.stip.iptransaction.model.ApiOrderResponse
import com.stip.stip.iptransaction.model.TradeResponse
import com.stip.stip.iptransaction.api.IpTransactionService
import com.stip.stip.iphome.adapter.UnfilledOrderAdapter
import com.stip.stip.order.adapter.FilledOrderAdapter
import com.stip.stip.iphome.fragment.CancelConfirmDialogFragment
import com.stip.stip.signup.utils.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryManager(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val unfilledAdapter: UnfilledOrderAdapter,
    private val filledAdapter: FilledOrderAdapter,
    private val fragmentManager: FragmentManager,
    private val coroutineScope: CoroutineScope,
    private val orderDataCoordinator: com.stip.stip.order.coordinator.OrderDataCoordinator? = null
) {

    var isUnfilledTabSelected: Boolean = true
        private set

    private var isVisible: Boolean = false
    private var orderCache: Map<String, ApiOrderResponse> = emptyMap()
    companion object { private const val TAG = "OrderHistoryManager" }

    init {
        setupAdapters()
        setupFilterClickListeners()
        setupCancelButtonListener()
        unfilledAdapter.onSelectionChanged = { hasSelection ->
            updateCancelButtonState(hasSelection)
        }
    }

    private fun setupAdapters() {
        binding.recyclerViewUnfilledOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = unfilledAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
        binding.recyclerViewFilledOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = filledAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    fun applyFilter(types: List<String>, startDate: String, endDate: String) {
        Log.d("OrderHistoryManager", "필터 적용됨: $types / $startDate ~ $endDate")
    }

    fun setupFilterClickListeners() {
        binding.tabUnfilled.setOnClickListener { handleTabClick(true) }
        binding.tabFilled.setOnClickListener { handleTabClick(false) }
    }

    private fun setupCancelButtonListener() {
        binding.buttonCancelSelectedOrders.setOnClickListener {
            performCancelSelectedOrders()
        }
    }

    fun activate() {
        isVisible = true
        binding.unfilledFilledBoxRoot.visibility = View.VISIBLE
        updateHistoryFilterTabAppearance()
        forceLoadCurrentTab()
    }

    private fun forceLoadCurrentTab() {
        if (!isVisible) return
        
        val isLoggedIn = PreferenceUtil.isRealLoggedIn()
        Log.d(TAG, "로그인 상태: $isLoggedIn")
        
        if (!isLoggedIn) {
            Log.d(TAG, "비로그인, 내역 안 보여")
            hideHistoryViews()
            return
        }

        if (isUnfilledTabSelected) {
            binding.recyclerViewFilledOrders.visibility = View.GONE
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_unfilled_orders)
            loadUnfilledOrders()
        } else {
            hideHistoryViews()
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_filled_orders)
            loadFilledOrders()
        }
    }

    fun hide() {
        isVisible = false
        binding.unfilledFilledBoxRoot.visibility = View.GONE
        hideHistoryViews()
    }

    fun handleTabClick(isUnfilledClicked: Boolean) {
        if (!isVisible) return
        if (isUnfilledClicked == isUnfilledTabSelected && binding.unfilledFilledBoxRoot.visibility == View.VISIBLE) return

        isUnfilledTabSelected = isUnfilledClicked
        updateHistoryFilterTabAppearance()
        
        val isLoggedIn = PreferenceUtil.isRealLoggedIn()
        Log.d(TAG, "로그인 상태: $isLoggedIn")
        
        if (!isLoggedIn) {
            Log.d(TAG, "비로그인, 내역 안 보여")
            hideHistoryViews()
            return
        }

        if (isUnfilledTabSelected) {
            binding.recyclerViewFilledOrders.visibility = View.GONE
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_unfilled_orders)
            loadUnfilledOrders()
        } else {
            hideHistoryViews()
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_filled_orders)
            loadFilledOrders()
        }
    }

    private fun loadUnfilledOrders() {
        Log.d(TAG, "loadUnfilledOrders() called")
        
        // 현재 선택된 티커에 해당하는 marketPairId를 가져오기
        val currentTicker = orderDataCoordinator?.currentTicker
        val marketPairId = TradingDataHolder.ipListingItems
            .find { it.ticker == currentTicker }?.registrationNumber
        
        Log.d(TAG, "Current ticker: $currentTicker, marketPairId: $marketPairId")
        
        if (marketPairId.isNullOrEmpty()) {
            Log.e(TAG, "Market Pair ID is null or empty - cannot load orders")
            showEmptyUnfilledState()
            return
        }

        Log.d(TAG, "Calling API for unfilled orders with marketPairId: $marketPairId")
        
        IpTransactionService.getApiUnfilledOrdersByMarketPair(marketPairId) { apiOrders, error ->
            Log.d(TAG, "API response received - error: $error, orders count: ${apiOrders?.size}")
            
            binding.root.post {
                if (error != null) {
                    showEmptyUnfilledState()
                } else if (apiOrders != null) {
                    val unfilledOrders = convertApiOrdersToUnfilledOrders(apiOrders)
                    displayUnfilledOrders(unfilledOrders)
                } else {
                    showEmptyUnfilledState()
                }
            }
        }
    }

    private fun loadFilledOrders() {
        Log.d(TAG, "loadFilledOrders() called")
        
        // 현재 선택된 티커에 해당하는 marketPairId를 가져오기
        val currentTicker = orderDataCoordinator?.currentTicker
        val marketPairId = TradingDataHolder.ipListingItems
            .find { it.ticker == currentTicker }?.registrationNumber
        
        Log.d(TAG, "Current ticker: $currentTicker, marketPairId: $marketPairId")
        
        if (marketPairId.isNullOrEmpty()) {
            Log.e(TAG, "Market Pair ID is null or empty - cannot load orders")
            showEmptyFilledState()
            return
        }

        Log.d(TAG, "Calling API for filled orders with marketPairId: $marketPairId")
        
        // 먼저 모든 주문 정보를 가져와서 캐시 (미체결 + 체결)
        val allOrders = mutableListOf<ApiOrderResponse>()
        
        // 미체결 주문 가져오기
        IpTransactionService.getApiUnfilledOrdersByMarketPair(marketPairId) { unfilledOrders, unfilledError ->
            if (unfilledError == null && unfilledOrders != null) {
                // 현재 사용자의 주문만 필터링
                val currentUserId = PreferenceUtil.getUserId()
                val userUnfilledOrders = if (!currentUserId.isNullOrEmpty()) {
                    unfilledOrders.filter { it.member.id == currentUserId }
                } else {
                    emptyList()
                }
                allOrders.addAll(userUnfilledOrders)
            }
            
            // 체결된 주문도 가져오기
            IpTransactionService.getApiFilledOrdersByMarketPair(marketPairId) { filledOrders, filledError ->
                if (filledError == null && filledOrders != null) {
                    // 현재 사용자의 주문만 필터링
                    val currentUserId = PreferenceUtil.getUserId()
                    val userFilledOrders = if (!currentUserId.isNullOrEmpty()) {
                        filledOrders.filter { it.member.id == currentUserId }
                    } else {
                        emptyList()
                    }
                    allOrders.addAll(userFilledOrders)
                }
                
                // 주문 정보를 캐시
                orderCache = allOrders.associateBy { it.id }
                Log.d(TAG, "Order cache updated with ${orderCache.size} orders")
                
                // 체결 정보 가져오기
                loadTradesData(marketPairId)
            }
        }
            
    }
    
    /**
     * 체결 정보를 가져오는 별도 메서드
     */
    private fun loadTradesData(marketPairId: String) {
        IpTransactionService.getApiTradesByMarketPair(marketPairId) { trades, error ->
            Log.d(TAG, "API response received - error: $error, trades count: ${trades?.size}")
            
            binding.root.post {
                if (error != null) {
                    showEmptyFilledState()
                } else if (trades != null) {
                    // 현재 사용자의 체결 내역만 필터링
                    val currentUserId = PreferenceUtil.getUserId()
                    val userTrades = if (!currentUserId.isNullOrEmpty()) {
                        trades.filter { trade ->
                            // buyOrderId나 sellOrderId가 현재 사용자의 주문인지 확인
                            val buyOrder = orderCache[trade.buyOrderId]
                            val sellOrder = orderCache[trade.sellOrderId]
                            (buyOrder?.member?.id == currentUserId) || (sellOrder?.member?.id == currentUserId)
                        }
                    } else {
                        emptyList()
                    }
                    
                    val filledOrders = convertTradeResponseToFilledOrders(userTrades, marketPairId)
                    displayFilledOrders(filledOrders)
                } else {
                    showEmptyFilledState()
                }
            }
        }
    }

    private fun convertApiOrdersToUnfilledOrders(apiOrders: List<ApiOrderResponse>): List<UnfilledOrder> {
        return apiOrders.map { apiOrder ->
            val tradeType = if (apiOrder.type == "buy") "매수" else "매도"
            val orderTime = formatDateTime(apiOrder.createdAt)
            val unfilledQuantity = (apiOrder.quantity - apiOrder.filledQuantity).toString()
            
            UnfilledOrder(
                orderId = apiOrder.id,
                memberNumber = apiOrder.member.id,
                ticker = apiOrder.marketPair.symbol,
                tradeType = tradeType,
                watchPrice = "--",
                orderPrice = String.format("%.2f", apiOrder.price),
                orderQuantity = apiOrder.quantity.toString(),
                unfilledQuantity = unfilledQuantity,
                orderTime = orderTime,
                status = "미체결"
            )
        }
    }

    private fun convertApiOrdersToFilledOrders(apiOrders: List<ApiOrderResponse>): List<IpInvestmentItem> {
        return apiOrders.map { apiOrder ->
            val type = if (apiOrder.type == "buy") "매수" else "매도"
            val orderTime = formatDateTime(apiOrder.createdAt)
            val executionTime = formatDateTime(apiOrder.updatedAt)
            val amount = (apiOrder.price * apiOrder.filledQuantity).toString()
            
            IpInvestmentItem(
                type = type,
                name = apiOrder.marketPair.symbol,
                quantity = apiOrder.filledQuantity.toString(),
                unitPrice = String.format("%.2f", apiOrder.price),
                amount = String.format("%.2f", apiOrder.price * apiOrder.filledQuantity),
                fee = "0.00",
                settlement = String.format("%.2f", apiOrder.price * apiOrder.filledQuantity),
                orderTime = orderTime,
                executionTime = executionTime
            )
        }
    }
    
    private fun convertTradeResponseToFilledOrders(trades: List<TradeResponse>, marketPairId: String): List<IpInvestmentItem> {
        val currentUserId = PreferenceUtil.getUserId()
        
        return trades.map { trade ->
            // marketPairId를 사용하여 심볼 정보 가져오기
            val symbol = getPairSymbol(marketPairId)
            val baseAsset = symbol.substringBefore("/")
            
            // 체결 시간을 주문 시간과 실행 시간으로 사용
            val orderTime = formatDateTime(trade.timestamp)
            val executionTime = formatDateTime(trade.timestamp)
            
            // 매수/매도 구분 - 캐시된 주문 정보를 사용하여 매수/매도 구분
            val type = getOrderTypeFromCache(trade.buyOrderId, trade.sellOrderId)
            
            IpInvestmentItem(
                type = type,
                name = baseAsset,
                quantity = trade.quantity.toString(),
                unitPrice = String.format("%.2f", trade.price),
                amount = String.format("%.2f", trade.price * trade.quantity),
                fee = "0.00",
                settlement = String.format("%.2f", trade.price * trade.quantity),
                orderTime = orderTime,
                executionTime = executionTime
            )
        }
    }
    
    /**
     * 캐시된 주문 정보를 사용하여 매수/매도 구분을 확인하는 메서드
     */
    private fun getOrderTypeFromCache(buyOrderId: String, sellOrderId: String): String {
        val currentUserId = PreferenceUtil.getUserId()
        
        // buyOrderId로 주문 정보 확인
        val buyOrder = orderCache[buyOrderId]
        if (buyOrder?.member?.id == currentUserId) {
            return if (buyOrder?.type == "buy") "매수" else "매도"
        }
        
        // sellOrderId로 주문 정보 확인
        val sellOrder = orderCache[sellOrderId]
        if (sellOrder?.member?.id == currentUserId) {
            return if (sellOrder?.type == "buy") "매수" else "매도"
        }
        
        // 기본값으로 "매수" 반환
        return "매수"
    }

    private fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateString", e)
            "00:00:00"
        }
    }

    private fun getPairSymbol(pairId: String): String {
        Log.d(TAG, "Converting pairId to symbol: $pairId")
        
        val ipItem = TradingDataHolder.ipListingItems.find { it.registrationNumber == pairId }
        
        return if (ipItem != null) {
            ipItem.ticker
        } else {
            if (pairId.length >= 8) {
                "IP-${pairId.substring(0, 8)}"
            } else {
                "IP-${pairId}"
            }
        }
    }

    private fun displayUnfilledOrders(orders: List<UnfilledOrder>) {
        val hasData = orders.isNotEmpty()
        binding.recyclerViewUnfilledOrders.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.textNoUnfilledOrders.visibility = if (!hasData) View.VISIBLE else View.GONE

        if (hasData) {
            unfilledAdapter.submitList(orders)
            updateCancelButtonState(unfilledAdapter.hasCheckedItems())
        }
    }

    private fun displayFilledOrders(orders: List<IpInvestmentItem>) {
        val hasData = orders.isNotEmpty()
        binding.recyclerViewFilledOrders.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.textNoUnfilledOrders.visibility = if (!hasData) View.VISIBLE else View.GONE

        if (hasData) {
            filledAdapter.submitList(orders)
        }
    }

    private fun showEmptyUnfilledState() {
        binding.recyclerViewUnfilledOrders.visibility = View.GONE
        binding.textNoUnfilledOrders.visibility = View.VISIBLE
        binding.textNoUnfilledOrders.text = context.getString(R.string.no_unfilled_orders)
        unfilledAdapter.submitList(emptyList())
        updateCancelButtonState(false)
    }

    private fun showEmptyFilledState() {
        binding.recyclerViewFilledOrders.visibility = View.GONE
        binding.textNoUnfilledOrders.visibility = View.VISIBLE
        binding.textNoUnfilledOrders.text = context.getString(R.string.no_filled_orders)
        filledAdapter.submitList(emptyList())
    }

    fun updateHistoryFilterTabAppearance() {
        if(!isVisible) return
        val activeColor = ContextCompat.getColor(context, R.color.main_point)
        val inactiveColor = ContextCompat.getColor(context, R.color.color_text_default)
        val selectedBgRes = R.drawable.bg_tab_unfilled_selected
        val unselectedBgRes = R.drawable.bg_tab_unselected
        val activeTypeface = Typeface.BOLD
        val inactiveTypeface = Typeface.NORMAL

        binding.tabUnfilled.setBackgroundResource(if (isUnfilledTabSelected) selectedBgRes else unselectedBgRes)
        binding.textTabUnfilled.setTextColor(if (isUnfilledTabSelected) activeColor else inactiveColor)
        binding.textTabUnfilled.setTypeface(null, if (isUnfilledTabSelected) activeTypeface else inactiveTypeface)

        binding.tabFilled.setBackgroundResource(if (!isUnfilledTabSelected) selectedBgRes else unselectedBgRes)
        binding.textTabFilled.setTextColor(if (!isUnfilledTabSelected) activeColor else inactiveColor)
        binding.textTabFilled.setTypeface(null, if (!isUnfilledTabSelected) activeTypeface else inactiveTypeface)
    }

    fun updateCancelButtonState(hasSelection: Boolean) {
        val hasData = unfilledAdapter.itemCount > 1
        val shouldBeVisible = isVisible && isUnfilledTabSelected && hasData

        binding.buttonCancelSelectedOrders.visibility = if (shouldBeVisible) View.VISIBLE else View.GONE

        if (binding.buttonCancelSelectedOrders.visibility == View.VISIBLE) {
            binding.buttonCancelSelectedOrders.isEnabled = hasSelection
            val bgColorRes = if (hasSelection) R.color.main_point else R.color.button_disabled_grey
            val textColorRes = if (hasSelection) R.color.white else R.color.text_disabled_grey
            try {
                binding.buttonCancelSelectedOrders.setBackgroundColor(ContextCompat.getColor(context, bgColorRes))
                binding.buttonCancelSelectedOrders.setTextColor(ContextCompat.getColor(context, textColorRes))
            } catch (e: Exception) {
                Log.e(TAG, "Error setting cancel button colors", e)
            }
        } else {
            binding.buttonCancelSelectedOrders.isEnabled = false
        }
    }

    private fun performCancelSelectedOrders() {
        val selectedOrderIds = unfilledAdapter.getSelectedOrderIds()

        if (selectedOrderIds.isEmpty()) {
            Toast.makeText(context, R.string.toast_select_orders_to_cancel, Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Showing custom cancel confirmation dialog for orders: $selectedOrderIds")

        val dialogMessage = try {
            context.getString(R.string.dialog_message_cancel_order_confirm_count, selectedOrderIds.size)
        } catch (e: Exception) {
            Log.w(TAG, "String resource 'dialog_message_cancel_order_confirm_count' not found or format error. Using default.", e)
            context.getString(R.string.dialog_message_cancel_order_confirm)
        }

        val cancelDialog = CancelConfirmDialogFragment.newInstance(
            R.string.dialog_title_cancel_order_confirm,
            dialogMessage
        )
        cancelDialog.show(fragmentManager, CancelConfirmDialogFragment.TAG)
    }

    fun loadFilledOrdersIfNeeded() {
        if (isVisible && !isUnfilledTabSelected) {
            loadFilledOrders()
        }
    }

    fun hideHistoryViews() {
        binding.recyclerViewUnfilledOrders.visibility = View.GONE
        binding.recyclerViewFilledOrders.visibility = View.GONE
        binding.textNoUnfilledOrders.visibility = View.GONE
        binding.buttonCancelSelectedOrders.visibility = View.GONE
    }
    
    /**
     * 미체결 주문 목록을 강제로 새로고침합니다.
     */
    fun forceRefreshUnfilledOrders() {
        Log.d(TAG, "미체결 주문 목록 강제 새로고침")
        
        // 어댑터 초기화
        unfilledAdapter.submitList(emptyList())
        unfilledAdapter.clearSelection()
        
        // 로딩 상태 표시
        binding.textNoUnfilledOrders.text = "로딩 중..."
        binding.textNoUnfilledOrders.visibility = View.VISIBLE
        binding.recyclerViewUnfilledOrders.visibility = View.GONE
        
        // 강제로 API 재호출
        loadUnfilledOrders()
        
        // 취소 버튼 상태 업데이트
        updateCancelButtonState(false)
    }
}
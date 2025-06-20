package com.stip.stip.order

import android.content.Context
import android.util.Log
import android.view.View
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iphome.util.OrderUtils
import com.stip.stip.signup.utils.PreferenceUtil

class OrderUIStateManager(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val orderDataCoordinator: OrderDataCoordinator,
    private val historyManager: OrderHistoryManager, // <<<--- 수정된 타입
    private val uiInitializer: OrderUIInitializer
) {
    private lateinit var orderEntryViews: List<View>
    private var currentTabPosition: Int = 0

    companion object {
        private const val TAG = "OrderUIStateManager"
    }

    init {
        initializeOrderEntryViews()
        handleTabSelection(binding.tabLayoutOrderMode.selectedTabPosition, true)
    }

    private fun initializeOrderEntryViews() {
        orderEntryViews = listOfNotNull(
            binding.radioGroupOrderType,
            binding.rowOrderAvailable,
            binding.orderInputContainer,
            binding.buttonContainer,
            binding.tradingInfoView?.root
        )
    }

    fun handleTabSelection(position: Int, isInitialSetup: Boolean = false) {
        // if (!isInitialSetup && position == currentTabPosition) return // isAdded 체크 추가 고려
        currentTabPosition = position

        val isHistoryTab = position == 2
        binding.unfilledFilledBoxRoot.visibility = if (isHistoryTab) View.VISIBLE else View.GONE
        setOrderEntryViewsVisibility(if (isHistoryTab) View.GONE else View.VISIBLE)

        try { // historyManager 메서드 호출 시 발생 가능한 오류 처리
            if (isHistoryTab) {
                historyManager.activate() // activate 호출 확인
            } else {
                historyManager.hide() // hide 호출 확인
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling historyManager activate/hide", e)
        }


        updateTradingInfoVisibility()
        uiInitializer.updateBuySellButtonAppearance(position)
        updateOrderAvailableDisplay()
    }

    private fun setOrderEntryViewsVisibility(visibility: Int) {
        try {
            if (::orderEntryViews.isInitialized) {
                orderEntryViews.forEach { it.visibility = visibility }
            } else {
                Log.w(TAG, "orderEntryViews not initialized before setOrderEntryViewsVisibility call")
            }
        } catch (e: Exception) { // Catch potential exceptions like IllegalStateException
            Log.e(TAG, "Error setting visibility", e)
        }
    }

    fun updateTradingInfoVisibility() {
        try {
            val infoLayout = binding.tradingInfoView ?: return
            val holds = orderDataCoordinator.userHoldsAsset
            
            // Check if user is logged in
            val isLoggedIn = PreferenceUtil.getMemberInfo() != null
            
            if (!isLoggedIn) {
                // If not logged in, hide all trading info
                infoLayout.groupValuationInfo.visibility = View.GONE
                infoLayout.dividerLine.visibility = View.GONE
                infoLayout.groupOrderInfo.visibility = View.GONE
                return
            }
            
            if (currentTabPosition == 0 || currentTabPosition == 1) {
                val visibility = if (holds) View.VISIBLE else View.GONE
                infoLayout.groupValuationInfo.visibility = visibility
                infoLayout.dividerLine.visibility = visibility
                infoLayout.groupOrderInfo.visibility = View.VISIBLE
            } else {
                infoLayout.groupValuationInfo.visibility = View.GONE
                infoLayout.dividerLine.visibility = View.GONE
                infoLayout.groupOrderInfo.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTradingInfoVisibility", e)
        }
    }

    fun updateOrderAvailableDisplay() {
        try {
            // 내역 탭(position == 2)인 경우 항상 주문가능 텍스트 숨김
            if (currentTabPosition == 2) {
                binding.rowOrderAvailable.visibility = View.GONE
                return
            }
            
            // 로그인 상태 확인
            val isLoggedIn = PreferenceUtil.getMemberInfo() != null
            
            if (!isLoggedIn) {
                // 로그인하지 않은 경우 모든 값을 0으로 표시 (매수/매도 탭에서만)
                binding.textOrderAvailableAmount.text = OrderUtils.fixedTwoDecimalFormatter.format(0.0)
                binding.textOrderAvailableUnit.text = if (currentTabPosition == 0) {
                    context.getString(R.string.unit_usd)
                } else {
                    orderDataCoordinator.currentTicker ?: "--"
                }
                binding.rowOrderAvailable.visibility = View.VISIBLE
                return
            }
            
            when (currentTabPosition) {
                0 -> {
                    val actualBuyableAmount = orderDataCoordinator.getActualBuyableAmount()
                    binding.textOrderAvailableAmount.text = OrderUtils.fixedTwoDecimalFormatter.format(actualBuyableAmount.toDouble())
                    binding.textOrderAvailableUnit.text = context.getString(R.string.unit_usd)
                    binding.rowOrderAvailable.visibility = if (orderDataCoordinator.availableUsdBalance > 0.0) View.VISIBLE else View.GONE
                }
                1 -> {
                    val actualHeldQuantity = orderDataCoordinator.getActualSellableQuantity()
                    val tickerName = orderDataCoordinator.currentTicker ?: "--"
                    if (actualHeldQuantity > 0) {
                        binding.textOrderAvailableAmount.text = OrderUtils.fixedTwoDecimalFormatter.format(actualHeldQuantity)
                        binding.textOrderAvailableUnit.text = tickerName
                    } else {
                        binding.textOrderAvailableAmount.text = OrderUtils.fixedTwoDecimalFormatter.format(0.0)
                        binding.textOrderAvailableUnit.text = tickerName
                    }
                    binding.rowOrderAvailable.visibility = View.VISIBLE
                }
                else -> {
                    binding.rowOrderAvailable.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOrderAvailableDisplay error", e)
            try { binding.rowOrderAvailable.visibility = View.GONE } catch (_: Exception) {}
        }
    }
}
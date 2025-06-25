package com.stip.stip.order

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager // FragmentManager import 추가
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentOrderContentBinding
import com.stip.stip.iptransaction.model.IpInvestmentItem
import com.stip.stip.iptransaction.model.UnfilledOrder
import com.stip.stip.iphome.adapter.UnfilledOrderAdapter
import com.stip.stip.FilledOrderAdapter
// MaterialAlertDialogBuilder import는 삭제된 상태 유지
import com.stip.stip.iphome.fragment.CancelConfirmDialogFragment // ❌️ 커스텀 다이얼로그 import (경로 확인 및 수정 필요)
import com.stip.stip.signup.utils.PreferenceUtil
import java.util.Random

class OrderHistoryManager(
    private val context: Context,
    private val binding: FragmentOrderContentBinding,
    private val unfilledAdapter: UnfilledOrderAdapter,
    private val filledAdapter: FilledOrderAdapter,
    private val fragmentManager: FragmentManager // 생성자에 FragmentManager 포함
) {

    // isUnfilledTabSelected 접근성은 public 또는 public getter로 유지
    var isUnfilledTabSelected: Boolean = true
        private set

    private var isVisible: Boolean = false
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
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = filledAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }


    fun applyFilter(types: List<String>, startDate: String, endDate: String) {
        // TODO: 실제 필터 로직 구현
        Log.d("OrderHistoryManager", "필터 적용됨: $types / $startDate ~ $endDate")

        // 예시: adapter에 필터링된 데이터 적용
        // 필터링 조건에 따라 dummyData.filter { ... } 형태로 구성 가능
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
        handleTabClick(isUnfilledTabSelected)
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
        
        // 로그인 상태 확인
        val isLoggedIn = PreferenceUtil.getMemberInfo() != null
        
        if (!isLoggedIn) {
            // 비로그인 상태일 때는 데이터를 보여주지 않고 아무것도 표시하지 않음
            hideHistoryViews()
            return
        }

        if (isUnfilledTabSelected) {
            binding.recyclerViewHistory.visibility = View.GONE
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_unfilled_orders)
            loadUnfilledOrders()
        } else {
            hideHistoryViews()
            binding.textNoUnfilledOrders.text = context.getString(R.string.no_filled_orders)
            loadFilledOrders()
        }
    }


    private fun loadUnfilledOrders() {
        // 더미 데이터 로딩 (변경 없음)
        val allDummyUnfilledOrders = listOf(
            UnfilledOrder("dummy_unfilled_1", com.stip.stip.signup.utils.PreferenceUtil.getString("PREF_KEY_COMMON_NUMBER"), "AXNO", "매수", "--", "15.50", "100.00", "50.00", "14:20:05"),
            UnfilledOrder("dummy_unfilled_2", com.stip.stip.signup.utils.PreferenceUtil.getString("PREF_KEY_COMMON_NUMBER"), "MSK", "매도", "30.00", "31.00", "20.00", "10.00", "11:15:30"),
            UnfilledOrder("dummy_unfilled_3", com.stip.stip.signup.utils.PreferenceUtil.getString("PREF_KEY_COMMON_NUMBER"), "", "매수", "--", "15.40", "200.00", "200.00", "09:05:10"),
            UnfilledOrder("dummy_unfilled_4", com.stip.stip.signup.utils.PreferenceUtil.getString("PREF_KEY_COMMON_NUMBER"), "MDM", "매도", "--", "19.30", "80.00", "40.00", "10:45:00"),
            UnfilledOrder("dummy_unfilled_5", com.stip.stip.signup.utils.PreferenceUtil.getString("PREF_KEY_COMMON_NUMBER"), "CDM", "매수", "--", "14.10", "120.00", "60.00", "13:12:15")
        )
        val randomCount = try { Random().nextInt(allDummyUnfilledOrders.size + 1) } catch (e: Exception) { 3 }
        val randomizedList = allDummyUnfilledOrders.shuffled().take(randomCount)
        val hasData = randomizedList.isNotEmpty()

        binding.recyclerViewUnfilledOrders.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.textNoUnfilledOrders.visibility = if (!hasData) View.VISIBLE else View.GONE

        unfilledAdapter.submitList(randomizedList)
        updateCancelButtonState(unfilledAdapter.hasCheckedItems())
    }

    private fun loadFilledOrders() {
        // 더미 데이터 로딩 (변경 없음)
        val dummyFilledOrders = listOf(
            IpInvestmentItem("매수", "AXNO", "12.00", "10.50", "126.00", "0.00", "126.00", "14:21:00", "2025.04.24 14:22"),
            IpInvestmentItem("매도", "CDM", "8.00", "11.00", "88.00", "0.00", "88.00", "13:11:00", "2025.04.24 13:12"),
            IpInvestmentItem("매수", "", "5.50", "12.75", "70.12", "0.00", "70.12", "11:20:00", "2025.04.24 11:21")
        )
        val randomCount = try { Random().nextInt(dummyFilledOrders.size + 1) } catch (e: Exception) { 2 }
        val randomizedList = dummyFilledOrders.shuffled().take(randomCount)
        val hasData = randomizedList.isNotEmpty()

        binding.recyclerViewHistory.visibility = if (hasData) View.VISIBLE else View.GONE
        binding.textNoUnfilledOrders.visibility = if (!hasData) View.VISIBLE else View.GONE

        filledAdapter.submitList(randomizedList)
    }

    fun updateHistoryFilterTabAppearance() {
        // UI 업데이트 로직 (변경 없음)
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
        // 버튼 상태 업데이트 로직 (변경 없음)
        val hasData = unfilledAdapter.itemCount > 1 // 헤더 제외
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

    // --- ▼▼▼ performCancelSelectedOrders 함수 수정됨 ▼▼▼ ---
    private fun performCancelSelectedOrders() {
        val selectedOrderIds = unfilledAdapter.getSelectedOrderIds()

        if (selectedOrderIds.isEmpty()) {
            Toast.makeText(context, R.string.toast_select_orders_to_cancel, Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Showing custom cancel confirmation dialog for orders: $selectedOrderIds")

        // --- ▼▼▼ 커스텀 DialogFragment 호출 ▼▼▼ ---
        // strings.xml에 dialog_message_cancel_order_confirm_count 같은 형식 지정자 포함 문자열 필요
        // 예: <string name="dialog_message_cancel_order_confirm_count">선택한 %d개의 주문을 취소하시겠습니까?</string>
        val dialogMessage = try {
            // 선택된 주문 개수를 메시지에 포함
            context.getString(R.string.dialog_message_cancel_order_confirm_count, selectedOrderIds.size)
        } catch (e: Exception) {
            // 리소스가 없거나 형식이 잘못된 경우 기본 메시지 사용 (strings.xml에 정의 필요)
            Log.w(TAG, "String resource 'dialog_message_cancel_order_confirm_count' not found or format error. Using default.", e)
            context.getString(R.string.dialog_message_cancel_order_confirm) // 기본 메시지 ID
        }

        // CancelConfirmDialogFragment의 newInstance를 사용하여 생성 및 데이터 전달
        val cancelDialog = CancelConfirmDialogFragment.newInstance(
            R.string.dialog_title_cancel_order_confirm, // 제목 리소스 ID
            dialogMessage // 위에서 생성된 메시지 문자열
        )
        // 생성자에서 전달받은 fragmentManager 사용
        cancelDialog.show(fragmentManager, CancelConfirmDialogFragment.TAG)
        // --- ▲▲▲ 커스텀 DialogFragment 호출 ▲▲▲ ---

        // ❗️ 중요: 실제 취소 로직(API 호출, UI 업데이트 등)은 이 함수가 아니라
        // OrderContentViewFragment에 설정된 FragmentResultListener에서 처리됩니다.
    }
    // --- ▲▲▲ performCancelSelectedOrders 함수 수정됨 ▲▲▲ ---

    fun loadFilledOrdersIfNeeded() {
        if (isVisible && !isUnfilledTabSelected) {
            loadFilledOrders()
        }
    }

    fun hideHistoryViews() {
        binding.recyclerViewUnfilledOrders.visibility = View.GONE
        binding.recyclerViewHistory.visibility = View.GONE
        binding.textNoUnfilledOrders.visibility = View.GONE
        binding.buttonCancelSelectedOrders.visibility = View.GONE
    }
    
    // 비로그인 상태 안내 메시지 표시 기능 삭제
}
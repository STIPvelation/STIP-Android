package com.stip.stip.iptransaction.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.ScrollableToTop
import com.stip.stip.databinding.FragmentIpUnfilledBinding
import com.stip.stip.iphome.adapter.UnfilledOrderAdapter
import com.stip.stip.iptransaction.api.IpTransactionService
import com.stip.stip.iptransaction.model.UnfilledOrder
import com.stip.stip.signup.utils.PreferenceUtil

class IpUnfilledFragment : Fragment(), ScrollableToTop {

    private lateinit var mainViewModel: com.stip.stip.MainViewModel

    private var _binding: FragmentIpUnfilledBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UnfilledOrderAdapter
    private var selectedOrderType: OrderType = OrderType.ALL

    enum class OrderType {
        ALL, NORMAL, RESERVED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpUnfilledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.stip.stip.MainViewModel::class.java]
        mainViewModel.memberInfo.observe(viewLifecycleOwner) { memberInfo ->
            // TODO: 회원정보를 UI에 반영하는 코드 작성
        }

        binding.filterContainerOrder.setOnClickListener {
            showOrderTypePopup(it)
        }

        binding.cancelOrderButton.setOnClickListener {
            if (::adapter.isInitialized && adapter.hasCheckedItems()) {
                val selectedIds = adapter.getSelectedOrderIds()

                Log.d("CancelOrder", "Cancelling ${selectedIds.size} items: ${selectedIds.joinToString()}")

                // Order cancellation message would show here
                adapter.clearSelection()
            } else {
                // No orders selected message would show here
            }
        }

        loadUnfilledOrders()

        updateOrderTypeText()
    }

    private fun loadUnfilledOrders() {
        // 로그인 여부 확인
        val memberId = PreferenceUtil.getMemberInfo()?.id
        if (memberId == null) {
            binding.nodatatext.text = "로그인이 필요합니다."
            binding.nodatatext.visibility = View.VISIBLE
            binding.recyclerViewUnfilled.visibility = View.GONE
            return
        }

        // 로딩 상태 표시
        // 프로그레스바가 없어 주석 처리
        // binding.progressBar.visibility = View.VISIBLE
        binding.nodatatext.visibility = View.GONE
        binding.recyclerViewUnfilled.visibility = View.GONE

        // API 호출
        IpTransactionService.getApiUnfilledOrders(
            memberId = memberId,
            page = 1,
            limit = 50
        ) { data, error ->
            requireActivity().runOnUiThread {
                // binding.progressBar.visibility = View.GONE // 프로그레스바 없음

                if (error != null) {
                    // 오류가 있어도 미체결 주문 없음 메시지 표시
                    showEmptyState()
                    return@runOnUiThread
                }

                // 데이터 표시
                if (data != null && data.isNotEmpty()) {
                    // ApiOrderResponse를 UnfilledOrder로 변환
                    val unfilledOrders = data.map { apiOrder ->
                        UnfilledOrder(
                            orderId = apiOrder.id,
                            memberNumber = apiOrder.userId,
                            ticker = apiOrder.pairId,
                            tradeType = if (apiOrder.type == "buy") "매수" else "매도",
                            watchPrice = apiOrder.price.toString(),
                            orderPrice = apiOrder.price.toString(),
                            orderQuantity = apiOrder.quantity.toString(),
                            unfilledQuantity = (apiOrder.quantity - apiOrder.filledQuantity).toString(),
                            orderTime = apiOrder.createdAt
                        )
                    }
                    setupUnfilledOrderList(unfilledOrders)
                } else {
                    showEmptyState()
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.nodatatext.visibility = View.VISIBLE
        binding.nodatatext.text = "미체결 주문이 없습니다."
        binding.recyclerViewUnfilled.visibility = View.GONE
    }

    private fun setupUnfilledOrderList(orderList: List<UnfilledOrder>) {
        if (orderList.isEmpty()) {
            binding.recyclerViewUnfilled.visibility = View.GONE
            binding.nodatatext.visibility = View.VISIBLE
        } else {
            binding.nodatatext.visibility = View.GONE
            binding.recyclerViewUnfilled.visibility = View.VISIBLE

            adapter = UnfilledOrderAdapter(orderList)
            adapter.onSelectionChanged = { isAnySelected ->
                activity?.runOnUiThread {
                    updateCancelButtonState(isAnySelected)
                }
            }

            binding.recyclerViewUnfilled.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewUnfilled.adapter = adapter
        }
        updateCancelButtonState(if (::adapter.isInitialized) adapter.hasCheckedItems() else false)
    }

    private fun updateCancelButtonState(isAnySelected: Boolean) {
        if (_binding == null || !isAdded) return
        val context = requireContext()
        binding.cancelOrderButton.isEnabled = isAnySelected

        if (isAnySelected) {
            binding.cancelOrderButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.main_point_blue)
            )
            binding.cancelOrderButton.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        } else {
            binding.cancelOrderButton.setBackgroundColor(
                ContextCompat.getColor(context, R.color.button_disabled_grey)
            )
            binding.cancelOrderButton.setTextColor(
                ContextCompat.getColor(context, R.color.text_disabled_grey)
            )
        }
    }

    private fun showOrderTypePopup(anchorView: View) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.order_type_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val allOrders = popupView.findViewById<TextView>(R.id.order_type_all)
        val normalOrders = popupView.findViewById<TextView>(R.id.order_type_normal)
        val reservedOrders = popupView.findViewById<TextView>(R.id.order_type_reserved)

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.main_point_blue)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

        fun applySelectedStyle() {
            allOrders.setTextColor(if (selectedOrderType == OrderType.ALL) selectedColor else defaultColor)
            normalOrders.setTextColor(if (selectedOrderType == OrderType.NORMAL) selectedColor else defaultColor)
            reservedOrders.setTextColor(if (selectedOrderType == OrderType.RESERVED) selectedColor else defaultColor)
        }

        applySelectedStyle()

        allOrders.setOnClickListener {
            selectedOrderType = OrderType.ALL
            updateOrderTypeText()
            popupWindow.dismiss()
        }
        normalOrders.setOnClickListener {
            selectedOrderType = OrderType.NORMAL
            updateOrderTypeText()
            popupWindow.dismiss()
        }
        reservedOrders.setOnClickListener {
            selectedOrderType = OrderType.RESERVED
            updateOrderTypeText()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchorView, 0, 10)
    }

    private fun updateOrderTypeText() {
        if (_binding == null || !isAdded) return
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.main_point_blue)
        binding.allOrdersText.setTextColor(selectedColor)
        binding.allOrdersText.text = when (selectedOrderType) {
            OrderType.ALL -> getString(R.string.all_orders)
            OrderType.NORMAL -> getString(R.string.order_type_normal)
            OrderType.RESERVED -> getString(R.string.order_type_reserved)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun scrollToTop() {
        if (_binding != null) {
            binding.recyclerViewUnfilled.scrollToPosition(0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = IpUnfilledFragment()
    }
}
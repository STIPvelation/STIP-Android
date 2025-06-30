package com.stip.stip.iphome.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.stip.stip.iphome.listener.InfoTabListener
import com.stip.stip.R
import com.stip.stip.databinding.FragmentTradingDetailBinding
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.iphome.model.IpListingItem
import java.text.DecimalFormat
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

// --- ✅ 1. InfoTabListener 인터페이스 구현 추가 ---
class TradingFragment : Fragment(), InfoTabListener {

    private var _binding: FragmentTradingDetailBinding? = null // ❗️ 실제 바인딩 클래스 이름 확인
    private val binding get() = _binding!!
    private var currentSelectedMenuId: Int = R.id.menu_item_order // 기본은 '주문' 메뉴


    private lateinit var marqueeHandler: Handler
    private lateinit var marqueeRunnable: Runnable
    private var marqueeIndex = 0
    private val marqueeInterval = 5000L


    private var currentTicker: String? = null
    private var companyName: String? = null

    private val fixedTwoDecimalFormatter = DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols =
            decimalFormatSymbols.apply { groupingSeparator = ','; decimalSeparator = '.' }
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }
    private val numberParseFormat = DecimalFormat.getNumberInstance(Locale.US) as DecimalFormat

    companion object {
        private const val ARG_TICKER = "ticker"
        private const val ARG_COMPANY_NAME = "companyName"

        fun newInstance(ticker: String, companyName: String): TradingFragment {
            return TradingFragment().apply {
                val bundle = arguments ?: Bundle()
                bundle.putString(ARG_TICKER, ticker)
                bundle.putString(ARG_COMPANY_NAME, companyName)
                arguments = bundle
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTicker = it.getString(ARG_TICKER)
            companyName = it.getString(ARG_COMPANY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentTradingDetailBinding.inflate(inflater, container, false) // ❗️ 실제 바인딩 클래스 사용
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val marqueeViews = listOf(
            binding.marqueeText1,
            binding.marqueeText2,
            binding.marqueeText3
        )

        marqueeViews.forEach { textView ->
            textView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val height = textView.height.toFloat()
                        val anim = TranslateAnimation(0f, 0f, height, -height).apply {
                            duration = 5000
                            repeatCount = Animation.INFINITE
                            repeatMode = Animation.RESTART
                        }
                        textView.startAnimation(anim)
                        textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
        }

        // ✅ 마르퀴 텍스트 순환 시작
        setupMarqueeText()

        // ✅ UI 초기화
        setupTopInfoAndPriceData()
        setupMenuClickListeners()

        if (savedInstanceState == null) {
            val selectedMenuId =
                arguments?.getInt("selectedMenuId", R.id.menu_item_order) ?: R.id.menu_item_order

            updateMenuSelection(selectedMenuId)

            when (selectedMenuId) {
                R.id.menu_item_order -> replaceFragment(
                    OrderContentViewFragment.newInstance(
                        currentTicker
                    )
                )

                R.id.menu_item_chart -> replaceFragment(TradingChartFragment.newInstance(currentTicker))
                R.id.menu_item_quotes -> replaceFragment(
                    IpHomeQuotesFragment.newInstance(
                        currentTicker
                    )
                )

                R.id.menu_item_info -> replaceFragment(IpHomeInfoFragment.newInstance(currentTicker))
            }
        }
    }

    private fun setupMarqueeText() {
        val ipList = TradingDataHolder.ipListingItems
        if (ipList.isEmpty()) return

        val textViews = listOf(
            binding.marqueeText1,
            binding.marqueeText2,
            binding.marqueeText3
        )

        marqueeHandler = Handler(Looper.getMainLooper())

        marqueeRunnable = object : Runnable {
            override fun run() {
                for (i in textViews.indices) {
                    val itemIndex = (marqueeIndex + i) % ipList.size
                    val item = ipList[itemIndex]

                    val price = item.currentPrice.toDoubleOrNull() ?: 0.0
                    val formattedPrice = String.format("$%,.2f", price)

                    // ✅ 티커 부분만 Bold 처리
                    val fullText = "${item.ticker} $formattedPrice"
                    val spannable = SpannableString(fullText)
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        item.ticker.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textViews[i].text = spannable
                }

                marqueeIndex = (marqueeIndex + 1) % ipList.size
                marqueeHandler.postDelayed(this, marqueeInterval)
            }
        }

        marqueeHandler.post(marqueeRunnable)
    }




    private fun setupTopInfoAndPriceData() {
        binding.textCompanyName.text = "${companyName ?: ""} ${currentTicker ?: ""}/USD"
        binding.icArrowIcon.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
        // TODO: more_options_icon 리스너

        val currentItemData =
            TradingDataHolder.ipListingItems.firstOrNull { it.ticker == currentTicker }

        if (currentItemData != null) {
            displayPriceInfo(currentItemData)
        } else {
            setDefaultPriceInfo()
        }

        binding.prevItemIndicator.setOnClickListener { navigateToSiblingTicker(-1) }
        binding.nextItemIndicator.setOnClickListener { navigateToSiblingTicker(1) }
    }

    private fun displayPriceInfo(item: IpListingItem) {
        if (_binding == null || !isAdded) return
        val ctx = context ?: return
        try {
            binding.currentPriceText.text = formatPrice(item.currentPrice)
            binding.percentageChangeText.text = item.changePercent
            binding.absoluteChangeText.text = formatPrice(item.changeAbsolute)
            binding.textVolumeValue24h.text = formatVolume(item.volume)
            binding.textHighValue24h.text = formatPrice(item.high24h)
            binding.textLowValue24h.text = formatPrice(item.low24h)

            val color = when {
                item.changePercent.startsWith("+") -> ContextCompat.getColor(
                    ctx,
                    R.color.percentage_positive_red
                )

                item.changePercent.startsWith("-") -> ContextCompat.getColor(
                    ctx,
                    R.color.percentage_negative_blue
                )

                else -> ContextCompat.getColor(ctx, R.color.text_primary)
            }
            binding.currentPriceText.setTextColor(color)
            binding.percentageChangeText.setTextColor(color)
            binding.absoluteChangeText.setTextColor(color)
            binding.textHighValue24h.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.percentage_positive_red
                )
            )
            binding.textLowValue24h.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.percentage_negative_blue
                )
            )

            val iconRes = when {
                item.changePercent.startsWith("+") -> R.drawable.ic_arrow_up_red
                item.changePercent.startsWith("-") -> R.drawable.ic_arrow_down_blue
                else -> 0
            }
            binding.changeIndicatorIcon.visibility = if (iconRes != 0) {
                binding.changeIndicatorIcon.setImageResource(iconRes); View.VISIBLE
            } else View.GONE

        } catch (e: Exception) {
            setDefaultPriceInfo()
        }
    }

    private fun setDefaultPriceInfo() {
        if (_binding == null || !isAdded) return
        binding.currentPriceText.text = "0.00"
        binding.percentageChangeText.text = "0.00%"
        binding.absoluteChangeText.text = "0.00"
        binding.textVolumeValue24h.text = "0"
        binding.textHighValue24h.text = "0.00"
        binding.textLowValue24h.text = "0.00"
        binding.changeIndicatorIcon.visibility = View.GONE
        context?.let {
            val defaultColor = ContextCompat.getColor(it, R.color.text_primary)
            binding.currentPriceText.setTextColor(defaultColor)
            binding.percentageChangeText.setTextColor(defaultColor)
            binding.absoluteChangeText.setTextColor(defaultColor)
            binding.textHighValue24h.setTextColor(defaultColor)
            binding.textLowValue24h.setTextColor(defaultColor)
        }
    }

    private fun formatPrice(priceString: String?): String {
        return try {
            if (priceString.isNullOrBlank()) return "0.00"
            val numberPart = priceString.replace(Regex("[^\\d.-]"), "")
            val number = numberParseFormat.parse(numberPart)?.toDouble() ?: 0.0
            val prefix =
                if (priceString.startsWith("-")) "-" else if (priceString.startsWith("+")) "+" else ""
            prefix + fixedTwoDecimalFormatter.format(Math.abs(number))
        } catch (e: Exception) {
            "0.00"
        }
    }

    private fun formatVolume(volumeString: String?): String {
        return try {
            if (volumeString.isNullOrBlank()) return "0"
            val numberPart = volumeString.replace(Regex("[^\\d]"), "")
            val number = numberPart.toLongOrNull() ?: 0L
            DecimalFormat("#,###").format(number)
        } catch (e: Exception) {
            "0"
        }
    }

    private fun navigateToSiblingTicker(offset: Int) {
        val fullIpList = TradingDataHolder.ipListingItems
        if (fullIpList.isEmpty()) return

        val currentIndex = fullIpList.indexOfFirst { it.ticker == currentTicker }
        if (currentIndex == -1) return

        val targetIndex = (currentIndex + offset + fullIpList.size) % fullIpList.size
        val targetItem = fullIpList[targetIndex]

        // 🔁 현재 TradingFragment 상태 유지한 채, 헤더와 내부 Fragment만 업데이트
        currentTicker = targetItem.ticker
        companyName = targetItem.companyName

        // ⬆️ 헤더 정보 업데이트
        binding.textCompanyName.text = "${companyName ?: ""} ${currentTicker ?: ""}/USD"
        displayPriceInfo(targetItem)

        // ⬇️ 현재 childFragment에 티커 업데이트 전달
        val currentChild = childFragmentManager.findFragmentById(R.id.trading_content_container)
        when (currentChild) {
            is OrderContentViewFragment -> currentChild.updateTicker(currentTicker)
            is TradingChartFragment -> currentChild.updateTicker(currentTicker)
            is IpHomeQuotesFragment -> currentChild.updateTicker(currentTicker)
            is IpHomeInfoFragment -> currentChild.updateTicker(currentTicker)
            is IpHomeInfoDetailFragment -> {
                // IpHomeInfoDetailFragment도 있다면 처리
                val item =
                    TradingDataHolder.ipListingItems.firstOrNull { it.ticker == currentTicker }
                item?.let { detailItem ->
                    val detailFragment = IpHomeInfoDetailFragment.newInstance(detailItem)
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.trading_content_container, detailFragment, "info_detail")
                    }
                }
            }
        }
    }


    private fun setupMenuClickListeners() {
        binding.menuItemOrder.setOnClickListener {
            replaceFragment(OrderContentViewFragment.newInstance(currentTicker))
            updateMenuSelection(it.id)
        }
        binding.menuItemChart.setOnClickListener {
            replaceFragment(TradingChartFragment.newInstance(currentTicker))
            updateMenuSelection(it.id)
        }
        binding.menuItemQuotes.setOnClickListener {
            replaceFragment(IpHomeQuotesFragment.newInstance(currentTicker))
            updateMenuSelection(it.id)
        }
        binding.menuItemInfo.setOnClickListener {
            // '정보' 메뉴 클릭 시 IpHomeInfoFragment 로드
            replaceFragment(IpHomeInfoFragment.newInstance(currentTicker))
            updateMenuSelection(it.id)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        // 이 TradingFragment 내부의 컨테이너 내용을 교체
        childFragmentManager.beginTransaction()
            .replace(
                R.id.trading_content_container,
                fragment
            ) // ❗️ 이 ID가 fragment_trading_detail.xml의 컨테이너 ID인지 확인
            .commit()
    }


    private fun updateMenuSelection(selectedMenuId: Int) {
        // ✅ 현재 선택된 메뉴 ID를 저장 (티커 이동 시에도 사용)
        currentSelectedMenuId = selectedMenuId

        // 🔹 모든 메뉴를 비활성화 스타일로 초기화
        binding.menuItemOrder.setTextAppearance(R.style.DefaultTextStyle_trading_inactive_14)
        binding.menuItemChart.setTextAppearance(R.style.DefaultTextStyle_trading_inactive_14)
        binding.menuItemQuotes.setTextAppearance(R.style.DefaultTextStyle_trading_inactive_14)
        binding.menuItemInfo.setTextAppearance(R.style.DefaultTextStyle_trading_inactive_14)

        // 🔹 선택된 메뉴만 활성화 스타일 적용
        val activeStyle = R.style.DefaultTextStyle_white_14
        when (selectedMenuId) {
            R.id.menu_item_order -> binding.menuItemOrder.setTextAppearance(activeStyle)
            R.id.menu_item_chart -> binding.menuItemChart.setTextAppearance(activeStyle)
            R.id.menu_item_quotes -> binding.menuItemQuotes.setTextAppearance(activeStyle)
            R.id.menu_item_info -> binding.menuItemInfo.setTextAppearance(activeStyle)
        }
    }

    override fun onDetailsTabSelected(ticker: String?) {
        if (ticker == null) return

        val item = TradingDataHolder.ipListingItems.firstOrNull { it.ticker == ticker }
        if (item != null) {
            val detailFragment = IpHomeInfoDetailFragment.newInstance(item)
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.trading_content_container, detailFragment, "info_detail")
                addToBackStack("info_detail")
            }
        } else {
            // 필요 시 로그 또는 기본 처리
            // Log.w("IpHomeInfoFragment", "No matching item found for ticker: $ticker")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        marqueeHandler.removeCallbacks(marqueeRunnable)
        _binding = null
    }
}
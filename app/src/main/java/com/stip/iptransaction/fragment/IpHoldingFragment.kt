package com.stip.stip.iptransaction.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.stip.ipasset.repository.IpAssetRepository
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.ScrollableToTop
import com.stip.stip.custom.MyDividerItemDecoration
import com.stip.stip.databinding.FragmentIpHoldingBinding
import com.stip.stip.iptransaction.api.IpTransactionService
import com.stip.stip.iptransaction.model.DipHoldingitem
import com.stip.stip.iptransaction.model.MyIpHoldingsSummaryItem
import com.stip.stip.signup.utils.PreferenceUtil
import com.github.mikephil.charting.components.Legend // 범례 설정 위해 추가
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
// import com.github.mikephil.charting.formatter.PercentFormatter // 사용 안 함
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.ArrayList

class IpHoldingFragment : Fragment(), ScrollableToTop {

    private lateinit var mainViewModel: com.stip.stip.MainViewModel
    private var _binding: FragmentIpHoldingBinding? = null
    private val binding get() = _binding!!

    private var isChartExpanded = true // 접기/펼치기 상태

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.stip.stip.MainViewModel::class.java]
        _binding = FragmentIpHoldingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.memberInfo.observe(viewLifecycleOwner) { memberInfo ->
            // TODO: 회원정보를 UI에 반영하는 코드 작성
        }

        // 자산 및 거래 내역 데이터 새로고침 이벤트 수신 설정
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                mainViewModel.refreshAppDataEvent.collect {
                    // 보유 잔고 데이터 새로고침
                    loadHoldingData()
                }
            }
        }
        
        // 포트폴리오 차트 접기/펼치기 기능 설정
        setupExpandCollapseAction()

        loadHoldingData()

        setupPieChart() // 수정된 범례 설정 반영
        updateChartVisibilityState() // 초기 차트 가시성 상태 설정
    }

    private fun loadHoldingData() {
        // 로그인 여부 확인
        val token = PreferenceUtil.getToken()
        if (token == null || token.isEmpty()) {
            binding.noipMessageText.text = "로그인이 필요합니다."
            binding.noipcurrently.visibility = View.VISIBLE
            binding.holdingsRecyclerView.visibility = View.GONE
            binding.summarySection.visibility = View.GONE
            return
        }
        
        // 로딩 표시
        // 프로그레스바 구현 필요 (현재 레이아웃에 없음)
        binding.noipMessageText.visibility = View.GONE
        binding.holdingsRecyclerView.visibility = View.GONE
        
        // 보유 요약 정보 API 호출
        IpTransactionService.getIpHoldingsSummary { summaryData, summaryError ->
            requireActivity().runOnUiThread {
                if (summaryError != null) {
                    // 오류 처리
                    // 프로그레스바 없음
                    binding.noipMessageText.visibility = View.VISIBLE
                    binding.noipMessageText.text = "데이터를 불러오는 중 오류가 발생했습니다."
                    return@runOnUiThread
                }
                
                // 요약 데이터 표시
                if (summaryData != null) {
                    // 요약 정보 표시 - setupSummaryInfo 메소드 호출
                    setupSummaryInfo(summaryData)
                    binding.summarySection.visibility = View.VISIBLE
                } else {
                    binding.summarySection.visibility = View.GONE
                }
                
                // 보유 상세 내역 API 호출
                IpTransactionService.getIpHoldings { holdingsData, holdingsError ->
                    requireActivity().runOnUiThread {
                        // 프로그레스바 없음
                        
                        if (holdingsError != null) {
                            // 오류 처리
                            binding.noipcurrently.visibility = View.VISIBLE
                            binding.noipMessageText.visibility = View.VISIBLE
                            binding.noipMessageText.text = "데이터를 불러오는 중 오류가 발생했습니다."
                            binding.holdingsRecyclerView.visibility = View.GONE
                            return@runOnUiThread
                        }
                        
                        // 보유 내역 표시
                        if (holdingsData != null && holdingsData.isNotEmpty()) {
                            val dipAdapter = DipHoldingsAdapter(holdingsData)
                            binding.holdingsRecyclerView.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = dipAdapter
                                addItemDecoration(MyDividerItemDecoration(requireContext()))
                            }
                            binding.holdingsRecyclerView.visibility = View.VISIBLE
                            binding.noipcurrently.visibility = View.GONE
                            loadPieChartData(holdingsData) // 수정된 라벨 생성 및 데이터 로드
                        } else {
                            binding.noipcurrently.visibility = View.VISIBLE
                            binding.noipMessageText.visibility = View.VISIBLE
                            binding.noipMessageText.text = "보유 중인 IP가 없습니다."
                            binding.holdingsRecyclerView.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    // 차트 가시성 및 아이콘 업데이트
    private fun updateChartVisibilityState() {
        if (isChartExpanded) {
            binding.portfolioPieChart.visibility = View.VISIBLE
            binding.ivExpandIcon.setImageResource(R.drawable.ic_arrow_up)
        } else {
            binding.portfolioPieChart.visibility = View.GONE
            binding.ivExpandIcon.setImageResource(R.drawable.ic_arrow_down)
        }
    }
    
    // 확장/축소 기능 구현
    private fun setupExpandCollapseAction() {
        binding.ivExpandIcon.setOnClickListener {
            isChartExpanded = !isChartExpanded
            updateChartVisibilityState()
        }
    }

    private fun setupSummaryInfo(summary: MyIpHoldingsSummaryItem) {
        // ... (기존 코드 유지) ...
        with(binding) {
            holdingUsdText.text = formatUsd(summary.holdingUsd)
            totalBuyText.text = formatUsd(summary.totalBuy)
            totalValuationText.text = formatUsd(summary.totalValuation)
            totalAssetText.text = formatUsd(summary.totalValuation)
            valuationProfitText.text = formatUsd(summary.valuationProfit)
            profitRateText.text = formatPercent(summary.profitRate)
            availableOrderText.text = formatUsd(summary.availableOrder)

            val isLoss = summary.valuationProfit < 0
            val colorRes = if (isLoss) R.color.color_fall else R.color.color_rise
            val color = ContextCompat.getColor(requireContext(), colorRes)
            valuationProfitText.setTextColor(color)
            profitRateText.setTextColor(color)
        }
    }

    private fun setupPieChart() {
        binding.portfolioPieChart.apply {
            isDrawHoleEnabled = true
            setUsePercentValues(false) // 퍼센트 값 자동 계산/표시 비활성화
            setEntryLabelTextSize(0f) // 차트 조각 라벨 숨김
            description.isEnabled = false
            setTouchEnabled(false)
            isRotationEnabled = false
            holeRadius = 65f
            transparentCircleRadius = 68f
            setHoleColor(Color.TRANSPARENT)
            setDrawCenterText(true)

            // 범례 설정 (오른쪽 기본값 또는 명시적 설정)
            legend.isEnabled = true
            // 기본값 사용 또는 필요시 명시적 설정
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT // 오른쪽
            legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER   // 수직 중앙
            legend.orientation = Legend.LegendOrientation.VERTICAL      // 세로 나열
            legend.setDrawInside(false)                                 // 차트 바깥쪽
            legend.isWordWrapEnabled = true                             // 긴 라벨 줄바꿈 허용
        }
    }

    // 데이터 로드 및 범례 라벨 수정
    private fun loadPieChartData(holdings: List<DipHoldingitem>) {
        val entries = ArrayList<PieEntry>()
        // 퍼센트 계산을 위한 전체 합계 계산
        val totalSum = holdings.sumOf { it.totalValuation }

        for (item in holdings) {
            if (item.totalValuation > 0) {
                // 해당 항목의 퍼센티지 계산
                val percentage = if (totalSum > 0) (item.totalValuation / totalSum) * 100 else 0.0
                // 퍼센티지 문자열 포맷 (소수점 첫째 자리까지)
                val percentageString = String.format("%.1f%%", percentage)
                // 범례에 표시될 최종 라벨 생성 ("이름  00.0%")
                val label = "${item.name}  ${percentageString}" // 이름과 퍼센트 사이에 공백 추가

                // PieEntry 생성 시 값과 *수정된 라벨* 사용
                entries.add(PieEntry(item.totalValuation.toFloat(), label))
            }
        }

        var shouldShowChart = false

        if (entries.isNotEmpty()) {
            shouldShowChart = true
            val dataSet = PieDataSet(entries, "") // 범례 그룹 제목은 비움
            dataSet.setColors(*ColorTemplate.MATERIAL_COLORS) // 색상 설정
            dataSet.setDrawValues(false) // *** 차트 조각 위에 값 표시 안 함 ***

            val data = PieData(dataSet)
            // data.setValueFormatter(...) // *** 값 포맷터 설정 필요 없음 ***

            binding.portfolioPieChart.data = data
            // 가운데 텍스트는 총 평가금액을 계속 표시 (totalSum 사용)
            binding.portfolioPieChart.centerText = generateCenterSpannableText(totalSum)
            binding.portfolioPieChart.invalidate()

        } else {
            shouldShowChart = false
            binding.portfolioPieChart.clear()
            binding.portfolioPieChart.invalidate()
        }

        // 최종 차트 가시성 결정
        if (!shouldShowChart) {
            isChartExpanded = false
        }
        updateChartVisibilityState() // 차트 데이터 로드 후 가시성 업데이트
    }

    // 가운데 텍스트 생성 함수 (총 평가금액 표시)
    private fun generateCenterSpannableText(totalValue: Double): SpannableString {
        val label = getString(R.string.center_label_total_value)
        val formattedTotalValue = formatUsd(totalValue)
        val text = "$label\n$formattedTotalValue"
        val spannableString = SpannableString(text)

        val labelStartIndex = 0
        val labelEndIndex = text.indexOf('\n')
        if (labelEndIndex > 0) {
            spannableString.setSpan(RelativeSizeSpan(0.8f), labelStartIndex, labelEndIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(Color.GRAY), labelStartIndex, labelEndIndex, 0)
        }

        val valueStartIndex = labelEndIndex + 1
        val valueEndIndex = text.length
        if (valueStartIndex < valueEndIndex) {
            spannableString.setSpan(RelativeSizeSpan(1.1f), valueStartIndex, valueEndIndex, 0)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), valueStartIndex, valueEndIndex, 0)
            spannableString.setSpan(ForegroundColorSpan(Color.BLACK), valueStartIndex, valueEndIndex, 0)
        }

        return spannableString
    }


    override fun scrollToTop() {
        binding.holdingsRecyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): IpHoldingFragment = IpHoldingFragment()
    }

    private fun formatUsd(value: Double): String {
        return "$" + String.format("%,.2f", value)
    }

    private fun formatPercent(value: Double): String {
        return String.format("%.2f%%", value)
    }

    private fun simulateDipHoldings(): Pair<List<DipHoldingitem>, MyIpHoldingsSummaryItem> {
        // ... (기존 코드 유지) ...
        val holdings = listOf(
            DipHoldingitem("WETALK", 10, 5.0, 52.5, 50.0, 2.5, 5.0),
            DipHoldingitem("MDM", 20, 2.5, 49.0, 50.0, -1.0, -2.0),
            DipHoldingitem("CDM", 5, 10.0, 55.0, 50.0, 5.0, 10.0),
            DipHoldingitem("", 5, 10.0, 55.0, 50.0, 5.0, 10.0)

        )
        val totalBuy = holdings.sumOf { it.totalBuyAmount }
        val totalValuation = holdings.sumOf { it.totalValuation }
        val valuationProfit = totalValuation - totalBuy
        val profitRate = if (totalBuy > 0) (valuationProfit / totalBuy) * 100 else 0.0

        val summary = MyIpHoldingsSummaryItem(
            holdingUsd = 100_000.0,
            totalBuy = totalBuy,
            totalValuation = totalValuation,
            valuationProfit = valuationProfit,
            profitRate = profitRate,
            availableOrder = 98_500.0
        )
        return holdings to summary
    }
}
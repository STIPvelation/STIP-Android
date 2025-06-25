package com.stip.stip.iphome.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.iphome.adapter.LicenseScopeAdapter
import com.stip.stip.iphome.adapter.LicenseScopeItem
import com.stip.stip.iphome.adapter.UsagePlanAdapter // UsagePlanAdapter import 확인
import com.stip.stip.databinding.FragmentIpHomeInfoBinding
import com.stip.stip.iphome.model.IpListingItem // IpListingItem import 확인
import com.stip.stip.iphome.TradingDataHolder
// DialogFragment import 확인

// --- OnUsageItemClickListener 인터페이스 구현 제거 ---
class IpHomeInfoFragment : Fragment() { // <<< 인터페이스 구현 제거

    private var _binding: FragmentIpHomeInfoBinding? = null
    private val binding get() = _binding!!

    private var currentTicker: String? = null
    private var currentItem: IpListingItem? = null
    private var isDetailTabActive: Boolean = false // '상세정보' 탭 활성화 여부

    // --- 어댑터 변수 (필요한 경우 유지, setup 함수 내에서만 사용하면 제거 가능) ---
    // private lateinit var usagePlanAdapter: UsagePlanAdapter
    // private lateinit var licenseScopeAdapter: LicenseScopeAdapter

    companion object {
        private const val ARG_TICKER = "ticker"
        const val TAG = "IpHomeInfoFragment" // Fragment Log Tag

        fun newInstance(ticker: String?): IpHomeInfoFragment {
            val fragment = IpHomeInfoFragment()
            val args = Bundle().apply {
                putString(ARG_TICKER, ticker)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTicker = it.getString(ARG_TICKER)
            Log.d(TAG, "onCreate: Received ticker = $currentTicker")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpHomeInfoBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: Binding inflated")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created for ticker = $currentTicker")
        updateUiForTicker(currentTicker) // UI 초기화 및 데이터 로드 (내부에서 Adapter 설정 호출)
        setupTabClickListeners()
        setupShortcutLinks()
        // setupLicenseScopeRecycler() // updateUiForTicker 에서 호출됨
        showInfoTab()
    }

    // Ticker에 해당하는 데이터로 UI 업데이트
    private fun updateUiForTicker(ticker: String?) {
        if (!isAdded || _binding == null) {
            Log.w(TAG, "updateUiForTicker: Fragment not added or binding is null. Ticker: $ticker")
            return
        }
        Log.d(TAG, "updateUiForTicker: Updating UI for ticker = $ticker")
        binding.tvTickerName.text = getString(R.string.ticker_name_format, ticker ?: "N/A")

        currentItem = TradingDataHolder.ipListingItems.firstOrNull { it.ticker == ticker }

        if (currentItem == null) {
            Log.w(TAG, "updateUiForTicker: No IpListingItem found for ticker = $ticker")
            // 데이터 없을 경우 UI 초기화
            binding.tvFirstIssuanceDate.text = "정보 없음"
            binding.tvTotalIssuanceLimit.text = "정보 없음"
            binding.registrationNumberBox.text = "정보 없음"
            binding.recyclerViewInfoDetails.adapter = null // ID 확인 필요
            binding.recyclerViewLicenseScope.adapter = null // ID 확인 필요
            // TODO: 사용자 알림 UI 처리 추가
            return
        }

        binding.tvFirstIssuanceDate.text = currentItem?.firstIssuanceDate ?: "정보 없음"
        binding.tvTotalIssuanceLimit.text = currentItem?.totalIssuanceLimit ?: "정보 없음"
        binding.registrationNumberBox.text = currentItem?.registrationNumber ?: "정보 없음"

        // 어댑터 설정 함수 호출
        setupUsagePlanRecycler(currentItem!!) // currentItem이 null 아님 보장
        setupLicenseScopeRecycler(currentItem!!)
    }

    // 바로가기 링크 설정
    private fun setupShortcutLinks() {
        binding.tvLinkBlockInquiry.setOnClickListener {
            // ★ IpListingItem 필드: linkBlock: String?
            openExternalUrl(currentItem?.linkBlock ?: "https://stipvelation.com/block")
        }



        binding.tvLinkIpRating.setOnClickListener {
            // currentItem 이 null 이 아닌지 먼저 확인
            currentItem?.let { item ->
                Log.d(TAG, "IP Rating link clicked for ticker: ${item.ticker}")
                // --- ✅ 새로운 newInstance 호출 방식으로 수정 ---
                val dialogFragment = RadarChartDialogFragment.newInstance(
                    // ★ IpListingItem 모델에서 실제 필드 이름 확인 필요
                    grade = item.patentGrade,          // 예: 등급 정보 필드
                    institutionalValues = item.institutionalValues,  // 예: 기관 평가 값 리스트 필드 (List<Float>?)
                    stipValues = item.stipValues,           // 예: STIP 평가 값 리스트 필드 (List<Float>?)
                    category = item.category             // 예: 카테고리 정보 필드
                )
                // --- ✅ 수정 끝 ---

                // 다이얼로그 표시 (childFragmentManager 사용 권장)
                dialogFragment.show(childFragmentManager, RadarChartDialogFragment.TAG)

            } ?: run {
                // currentItem이 null일 경우 오류 처리
                Log.w(TAG, "setupShortcutLinks: currentItem is null, cannot show IP Rating.")
                Toast.makeText(requireContext(), "IP 등급 정보를 표시할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLinkLicense.setOnClickListener {
            // ★ IpListingItem 필드: linkLicense: String?
            openExternalUrl(currentItem?.linkLicense ?: "https://stipvelation.com/license")
        }



        binding.tvLinkViewVideo.setOnClickListener {
            // ★ IpListingItem 필드: linkVideo: String?
            openExternalUrl(currentItem?.linkVideo ?: "https://stipvelation.com/video")
        }
    }

    // 외부 URL 열기
    private fun openExternalUrl(url: String?) {
        if (url.isNullOrBlank()) {
            Log.w(TAG, "openExternalUrl: URL is null or blank.")
            Toast.makeText(requireContext(), "유효하지 않은 URL입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            Log.d(TAG, "openExternalUrl: Opening URL: $url")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "openExternalUrl: Failed to open URL: $url", e)
            Toast.makeText(requireContext(), "URL을 여는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    // 탭 클릭 리스너 설정
    private fun setupTabClickListeners() {
        binding.textViewTabInfo.setOnClickListener { if (isDetailTabActive) showInfoTab() }
        binding.textViewTabDetails.setOnClickListener { if (!isDetailTabActive) showDetailsTab() }
    }

    // '기본정보' 탭 표시 로직
    private fun showInfoTab() {
        if (!isAdded) return
        Log.d(TAG, "showInfoTab: Showing Info Tab")
        isDetailTabActive = false
        updateTabVisuals(isInfoTab = true)

        // 상세정보 Fragment가 있다면 제거 (애니메이션 등 고려 시 다른 방식 사용 가능)
        childFragmentManager.findFragmentById(R.id.info_content_container)?.let { fragment ->
            if (fragment.isAdded) {
                Log.d(TAG, "showInfoTab: Removing existing detail fragment")
                childFragmentManager.commit { remove(fragment) }
            }
        } ?: Log.d(TAG, "showInfoTab: No fragment found to remove.")

        binding.infoContentContainer.visibility = View.GONE
        binding.basicContentGroup.visibility = View.VISIBLE // 기본 정보 그룹 ID 확인 필요
    }

    // '상세정보' 탭 표시 로직
    private fun showDetailsTab() {
        if (!isAdded) return
        Log.d(TAG, "showDetailsTab: Showing Details Tab")
        isDetailTabActive = true
        updateTabVisuals(isInfoTab = false)

        binding.basicContentGroup.visibility = View.GONE
        binding.infoContentContainer.visibility = View.VISIBLE

        currentItem?.let { item ->
            Log.d(
                TAG,
                "showDetailsTab: Replacing with IpHomeInfoDetailFragment for ticker=${item.ticker}"
            )
            replaceFragmentSafely(IpHomeInfoDetailFragment.newInstance(item))
        } ?: Log.w(TAG, "showDetailsTab: currentItem is null, cannot show details tab.")
    }

    // 탭 UI 업데이트
    private fun updateTabVisuals(isInfoTab: Boolean) {
        if (!isAdded || context == null) {
            Log.w(TAG, "updateTabVisuals: Cannot update visuals.")
            return
        }
        val activeColor = ContextCompat.getColor(requireContext(), R.color.tab_text_active)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.tab_text_inactive)

        binding.textViewTabInfo.setTextColor(if (isInfoTab) activeColor else inactiveColor)
        binding.underlineActiveInfo.visibility = if (isInfoTab) View.VISIBLE else View.GONE
        binding.textViewTabDetails.setTextColor(if (!isInfoTab) activeColor else inactiveColor)
        binding.underlineActiveDetails.visibility = if (!isInfoTab) View.VISIBLE else View.GONE
    }

    // Fragment 안전하게 교체
    private fun replaceFragmentSafely(fragment: Fragment) {
        if (!isAdded || _binding == null || !isResumed || childFragmentManager.isStateSaved) {
            Log.w(TAG, "replaceFragmentSafely: Cannot perform fragment transaction.")
            return
        }
        Log.d(TAG, "replaceFragmentSafely: Replacing fragment in R.id.info_content_container")
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.info_content_container, fragment) // 컨테이너 ID 확인 필요
        }
    }

    // 라이선스 범위 RecyclerView 설정
    private fun setupLicenseScopeRecycler(item: IpListingItem) {
        if (!isAdded || context == null) return

        val licenseScopeItems = listOf(
            LicenseScopeItem(
                title = getString(R.string.license_scope_default_title),
                entries = listOf(/* ... 예시 데이터 ... */)
            )
        )

        val adapter = LicenseScopeAdapter(licenseScopeItems)
        binding.recyclerViewLicenseScope.adapter = adapter
        if (binding.recyclerViewLicenseScope.layoutManager == null) {
            binding.recyclerViewLicenseScope.layoutManager = LinearLayoutManager(requireContext())
        }

        Log.d(TAG, "setupLicenseScopeRecycler: Adapter set for recyclerViewLicenseScope")
    }









    // 사용 계획 RecyclerView 설정
    private fun setupUsagePlanRecycler(item: IpListingItem) {
        if (!isAdded || context == null) {
            Log.w(TAG, "setupUsagePlanRecycler: Cannot setup.")
            return
        }

        Log.d(TAG, "setupUsagePlanRecycler: Setting up UsagePlanAdapter for ticker=${item.ticker}")

        val adapter = UsagePlanAdapter(
            item = item,
            onShowUsagePlanClick = {
                Log.d(TAG, "UsagePlanAdapter - onShowUsagePlanClick invoked!")
                UsagePlanDialogFragment.newInstance(
                    usagePlanData = item.usagePlanData,
                    ticker = item.ticker // ✅ 여기!
                ).show(childFragmentManager, UsagePlanDialogFragment.TAG)
            },
            onShowLicenseAgreementClick = {
                Log.d(TAG, "UsagePlanAdapter - onShowLicenseAgreementClick invoked!")
                LicenseAgreementDialogFragment.newInstance()
                    .show(childFragmentManager, LicenseAgreementDialogFragment.TAG)
            }
        )

        binding.recyclerViewInfoDetails.adapter = adapter

        if (binding.recyclerViewInfoDetails.layoutManager == null) {
            binding.recyclerViewInfoDetails.layoutManager = LinearLayoutManager(requireContext())
        }

        Log.d(TAG, "setupUsagePlanRecycler: Adapter set for recyclerViewInfoDetails")
    }


    // 외부(예: TradingFragment)에서 Ticker 변경 시 호출될 함수
    fun updateTicker(ticker: String?) {
        if (!isAdded || _binding == null) {
            Log.w(TAG, "updateTicker: Cannot update. New ticker: $ticker")
            return
        }
        Log.d(TAG, "updateTicker: Updating to new ticker = $ticker")
        currentTicker = ticker
        updateUiForTicker(ticker) // UI 및 데이터 업데이트 (내부에서 어댑터 설정 포함)

        // 탭 상태에 따라 컨텐츠 갱신
        if (isDetailTabActive) {
            showDetailsTab()
        } else {
            showInfoTab()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up binding for ticker = $currentTicker")
        _binding?.recyclerViewInfoDetails?.adapter = null // 안전하게 null 처리
        _binding?.recyclerViewLicenseScope?.adapter = null // 안전하게 null 처리
        _binding = null
    }
}
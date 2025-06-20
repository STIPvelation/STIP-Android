package com.stip.stip.more.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.api.repository.IpListingRepository
import com.stip.stip.databinding.FragmentMoreModeSettingBinding
import com.stip.stip.iphome.TradingDataHolder
import com.stip.stip.iphome.fragment.IpHomeFragment
import kotlinx.coroutines.launch
import java.util.*

class MoreModeSettingFragment : Fragment() {

    private var _binding: FragmentMoreModeSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private val TAG = "MoreModeSettingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreModeSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateHeaderTitle(getString(R.string.title_mode_setting))
        viewModel.enableBackNavigation {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        // IP 새로고침 버튼 클릭 이벤트 추가
        binding.btnIpRefresh.setOnClickListener {
            refreshIpListing()
            Toast.makeText(requireContext(), "IP 목록을 새로 갱신했습니다.", Toast.LENGTH_SHORT).show()
        }

        val sharedPref = requireContext().getSharedPreferences("mode_pref", Context.MODE_PRIVATE)
        val keepScreenOn = sharedPref.getBoolean("keep_screen_on", false)

        binding.switchScreenOn.isChecked = keepScreenOn
        setKeepScreenOnFlag(keepScreenOn)
        binding.switchScreenOn.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("keep_screen_on", isChecked).apply()
            setKeepScreenOnFlag(isChecked)
        }

        binding.switchDisplayNormal.apply {
            isChecked = true
            setOnTouchListener { _, _ -> true }
        }

        listOf(binding.switchDisplayDark, binding.switchDisplayDesign, binding.switchDisplayUser).forEach { switch ->
            switch.isChecked = false
            switch.isEnabled = true
            switch.setOnCheckedChangeListener { button, _ ->
                button.isChecked = false
                Toast.makeText(requireContext(), "해당 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        setupLanguageToggle()
    }

    private fun setupLanguageToggle() {
        val languageMap = mapOf(
            "ko" to binding.switchLanguageKorean,
            "en" to binding.switchLanguageEnglish,
            "ja" to binding.switchLanguageJapanese,
            "zh" to binding.switchLanguageChinese
        )

        // 현재 설정된 언어 확인
        val currentLang = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "ko"

        // 초기 상태 반영
        languageMap.forEach { (lang, switch) ->
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = (lang == currentLang)
        }

        // 리스너 설정
        languageMap.forEach { (lang, switch) ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    languageMap.forEach { (otherLang, otherSwitch) ->
                        if (otherLang != lang) {
                            otherSwitch.setOnCheckedChangeListener(null)
                            otherSwitch.isChecked = false
                        }
                    }

                    val newLocale = LocaleListCompat.forLanguageTags(lang)
                    AppCompatDelegate.setApplicationLocales(newLocale)

                    Toast.makeText(requireContext(), getString(R.string.language_changed), Toast.LENGTH_SHORT).show()

                    requireActivity().recreate() // 🔄 즉시 반영
                }
            }
        }
    }

    private fun setKeepScreenOnFlag(enabled: Boolean) {
        if (enabled) {
            requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.d(TAG, "화면 꺼짐 방지 ON")
        } else {
            requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            Log.d(TAG, "화면 꺼짐 방지 OFF")
        }
    }

    /**
     * IP 리스팅 데이터만 새로 갱신하는 함수
     * 프로그레스바 없이 배경에서 IP 데이터만 갱신함
     */
    private fun refreshIpListing() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 실제 API를 통해 최신 데이터 가져오기
                val repository = IpListingRepository()
                val updatedData = repository.getIpListing()
                
                if (updatedData.isNotEmpty()) {
                    // 전역 데이터 홀더에 업데이트된 데이터 저장
                    // ipListingItems가 불변 List 형식이므로 새 리스트로 할당
                    TradingDataHolder.ipListingItems = updatedData.toList()
                    
                    // 현재 IpHomeFragment가 있다면 그 중 데이터 갱신
                    val fragmentManager = requireActivity().supportFragmentManager
                    val ipHomeFragment = fragmentManager.fragments.filterIsInstance<IpHomeFragment>().firstOrNull()
                    
                    ipHomeFragment?.let {
                        // IpHomeFragment가 있는 경우 조용히 데이터만 갱신
                        it.refreshIpListingData()
                    }
                }
            } catch (e: Exception) {
                // 오류 발생 시 조용히 무시
                android.util.Log.e("MoreModeSettingFragment", "IP 리스팅 갱신 실패: ${e.message}")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: 바인딩 해제")
    }
}

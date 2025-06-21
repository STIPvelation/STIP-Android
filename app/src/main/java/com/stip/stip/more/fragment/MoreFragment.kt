package com.stip.stip.more.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreBinding
import com.stip.stip.more.*

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    private val activityViewModel: MainViewModel by activityViewModels()
    private val TAG = "MoreFragment_Debug"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            saveProfileImageUri(it)
            loadProfileWithGlide(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        setupClickListeners()
        loadSavedProfileImage()
        setAppVersionInfo()
        
        // 회원정보 관찰 및 표시
        activityViewModel.memberInfo.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                // 회원 이름 표시
                binding.textViewName.text = info.name

                // 회원 ID(9자리 공통번호) 표시
                if (info.name.isNotBlank()) {
                    binding.textViewId.text = info.name
                    binding.textViewId.visibility = View.VISIBLE
                    Log.d(TAG, "회원 ID 표시: ${info.id}")
                } else {
                    // 회원 ID가 없는 경우 UI에서 숨김 처리
                    binding.textViewId.visibility = View.GONE
                    Log.d(TAG, "회원 ID 없음 - 화면에서 숨김")
                }
            } else {
                // 비로그인 상태일 때
                binding.textViewName.text = "로그인이 필요합니다"
                binding.textViewId.visibility = View.GONE
                Log.d(TAG, "비로그인 상태 - 회원 ID 숨김")
            }
        }
        
        // API를 통해 회원정보 로드
        refreshMemberInfo()

        binding.imageViewProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }
    override fun onResume() {
        super.onResume()
        activityViewModel.updateHeaderTitle(getString(R.string.header_more))
        activityViewModel.updateNavigationIcon(0)
        Log.d(TAG, "onResume: Header title set to '더보기'")
        
        // 화면으로 돌아올 때마다 회원정보 새로고침
        refreshMemberInfo()
    }

    private fun setupClickListeners() {
        binding.profileSection.setOnClickListener {
            navigateTo(MoreMemberInfoFragment(), "회원 정보")
        }
        // 빠른 메뉴 버튼 클릭 리스너
        binding.textViewPriceAlert.setOnClickListener {
            navigateTo(MorePriceAlertFragment(), "시세 알림")
        }
        binding.textViewModeSettings.setOnClickListener {
            navigateTo(MoreModeSettingFragment(), "모드 설정")
        }
        binding.textViewSecurityAuth.setOnClickListener {
            navigateTo(MoreSecurityFragment(), "보안 / 인증")
        }
        // IP 엔터테인먼트 카드 클릭 리스너는 나중에 추가 예정
        // 현재 레이아웃에는 카드뷰에 ID가 설정되어 있지 않음
        
        // 정책 및 약관 카드 클릭 리스너
        binding.cardPolicy.setOnClickListener {
            // 정책 및 약관 화면으로 이동
            navigateTo(MorePolicyFragment(), "정책 및 약관")
        }
        
        binding.cardEsgPolicy.setOnClickListener {
            // ESG 정책 화면으로 이동
            Toast.makeText(requireContext(), "ESG 정책 페이지로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardStartupPolicy.setOnClickListener {
            // 스타트업 지원 정책 화면으로 이동
            Toast.makeText(requireContext(), "스타트업 지원 정책 페이지로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardAmlPolicy.setOnClickListener {
            // AML 정책 화면으로 이동
            Toast.makeText(requireContext(), "AML 정책 페이지로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardDipPolicy.setOnClickListener {
            // DIP 평가 기준 화면으로 이동
            Toast.makeText(requireContext(), "DIP 평가 기준 페이지로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardFeePolicy.setOnClickListener {
            // 수수료 정책 화면으로 이동
            Toast.makeText(requireContext(), "수수료 정책 페이지로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardStipvelation.setOnClickListener {
            // STIPvelation 외부 링크로 이동
            Toast.makeText(requireContext(), "STIPvelation 사이트로 이동합니다", Toast.LENGTH_SHORT).show()
        }
        
        // 고객센터 버튼 클릭 리스너
        binding.cardCustomerCenter.setOnClickListener {
            navigateTo(MoreCustomerCenterFragment(), "고객센터")
        }
    }

    private fun navigateTo(fragment: Fragment, logName: String) {
        Log.d(TAG, "$logName 클릭됨! 화면 이동 시작")
        try {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
            }
            Log.d(TAG, "$logName replace 완료")
        } catch (e: Exception) {
            Log.e(TAG, "$logName 화면 이동 실패!", e)
            Toast.makeText(context, "화면 전환 오류: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveProfileImageUri(uri: Uri) {
        val pref = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE)
        pref.edit().putString("profile_uri", uri.toString()).apply()
    }

    private fun loadSavedProfileImage() {
        val pref = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE)
        val uriString = pref.getString("profile_uri", null)
        uriString?.let {
            loadProfileWithGlide(Uri.parse(it))
        }
    }
    private fun loadProfileWithGlide(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .transform(CircleCrop())
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.imageViewProfile)
    }
    
    //새로고침
    private fun refreshMemberInfo() {
        // 로그인 상태 확인
        if (activityViewModel.isAuthenticated) {
            Log.d(TAG, "회원정보 API 조회")
            activityViewModel.loadMemberInfo()
        }
    }

    /**
     * 앱 버전 정보를 표시하는 함수
     */
    private fun setAppVersionInfo() {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            binding.textViewAppVersion.text = "앱 버전 $versionName"
        } catch (e: Exception) {
            Log.e(TAG, "앱 버전 정보를 가져오는데 실패했습니다: ${e.message}")
            binding.textViewAppVersion.text = "앱 버전 정보 없음"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }
}

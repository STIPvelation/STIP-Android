package com.stip.stip.more.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreMemberInfoBinding
import com.stip.stip.MainViewModel
import com.stip.stip.signup.login.LoginActivity
import com.stip.stip.signup.utils.PreferenceUtil
import com.stip.stip.ui.dialog.LogoutDialogFragment
import com.stip.stip.ui.dialog.AccountDeletionGuideFragment

class MoreMemberInfoFragment : Fragment() {

    private var _binding: FragmentMoreMemberInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreMemberInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 헤더 설정
        viewModel.updateHeaderTitle("회원 정보")
        viewModel.enableBackNavigation {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // API에서 회원정보 로드 (ViewModel에서 업데이트 시작)
        // loadMemberInfo()는 onStart() 또는 onResume()에서 호출할 수도 있음
        // 회원정보 관찰 및 UI 반영
        viewModel.memberInfo.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                // 프로필 섹션
                binding.textViewProfileName.text = info.name
                // 기본 회원정보
                binding.root.findViewById<TextView>(R.id.value_name).text = info.name
                binding.root.findViewById<TextView>(R.id.value_email).text = info.email
                
                // 핸드폰 번호 포맷팅
                val formattedPhoneNumber = com.stip.stip.signup.utils.Utils.formatPhoneNumber(info.phoneNumber)
                binding.root.findViewById<TextView>(R.id.value_phone).text = formattedPhoneNumber
                // 추가 필드 업데이트
                binding.root.findViewById<TextView>(R.id.value_passport_name).text =
                    info.englishFirstName + " " + info.englishLastName ?: "-"
                // 생년월일 포맷팅
                val formattedBirthdate = com.stip.stip.signup.utils.Utils.formatBirthdate(info.birthdate)
                binding.root.findViewById<TextView>(R.id.value_dob).text = formattedBirthdate
                // 은행 코드를 은행 이름으로 변환하여 표시
                val bankName = com.stip.stip.signup.utils.Utils.getBankNameByCode(requireContext(), info.bankCode)
                
                // 계좌번호 포맷팅
                val formattedAccountNumber = com.stip.stip.signup.utils.Utils.formatAccountNumber(info.bankCode, info.accountNumber)
                binding.root.findViewById<TextView>(R.id.value_bank).text = "$bankName $formattedAccountNumber"
                // 주소 포맷팅 (우편번호 포함)
                val formattedAddress = com.stip.stip.signup.utils.Utils.formatAddress(info.postalCode, info.address)
                binding.root.findViewById<TextView>(R.id.value_address).text = formattedAddress
                
                // 직업 코드를 직업명으로 변환하여 표시
                val jobName = com.stip.stip.signup.utils.Utils.getJobNameByCode(requireContext(), info.job)
                binding.root.findViewById<TextView>(R.id.value_job).text = jobName

                // 회원정보가 있는 경우 관련 버튼 활성화
                binding.textViewEditInfo.isEnabled = true
                binding.buttonLogout.isEnabled = true
                binding.textViewWithdraw.isEnabled = true
            } else {
                // 회원정보가 없는 경우 (로그인 필요)
                binding.textViewProfileName.text = "로그인이 필요합니다"

                // 모든 필드에 기본값 설정
                val allValueIds = listOf(
                    R.id.value_name, R.id.value_passport_name, R.id.value_email,
                    R.id.value_phone, R.id.value_dob, R.id.value_bank,
                    R.id.value_address, R.id.value_job
                )

                allValueIds.forEach { id ->
                    binding.root.findViewById<TextView>(id)?.apply {
                        text = "-"
                    }
                }

                // 관련 버튼 비활성화
                binding.textViewEditInfo.isEnabled = false
                binding.buttonLogout.isEnabled = false
                binding.textViewWithdraw.isEnabled = false

                // 로그인 화면으로 이동 처리 추가
                binding.profileSummarySection.setOnClickListener {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                }
            }
        }

        // API를 통해 회원정보 로드
        refreshMemberInfo()

        setupNavigationListener()
        setupLogoutListener()
        setupWithdrawDialogListener()
    }

    private fun setupNavigationListener() {
        binding.textViewEditInfo.setOnClickListener {
            // 회원정보가 있는지 확인 (isAuthenticated 대신 회원정보 존재 여부로 체크)
            if (viewModel.memberInfo.value != null) {
                // PIN 확인 화면으로 이동
                startActivity(
                    Intent(
                        requireContext(),
                        com.stip.stip.more.activity.PinVerificationActivity::class.java
                    )
                )
            } else {
                // 로그인 필요 메시지
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(
                        requireContext(),
                        com.stip.stip.signup.login.LoginActivity::class.java
                    )
                )
            }
        }
    }


    private fun setupLogoutListener() {
        binding.buttonLogout.setOnClickListener {
            LogoutDialogFragment {
                performLogout()
            }.show(parentFragmentManager, "LogoutDialog")
        }
    }

    private fun setupWithdrawDialogListener() {
        binding.textViewWithdraw.setOnClickListener {
            AccountDeletionGuideFragment {
                performAccountDeletion()
            }.show(parentFragmentManager, "AccountDeletionGuideDialog")
        }
    }

    private fun performLogout() {
        PreferenceUtil.clear()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    // 회원탈퇴 제한 상수
    companion object {
        // 기기 식별자를 이용해 동일 기기에서 탈퇴-재가입 횟수 추적
        private const val PREF_KEY_DEVICE_ID = "device_unique_id"
        private const val PREF_KEY_WITHDRAWAL_COUNT = "withdrawal_count_yearly"
        private const val PREF_KEY_LAST_WITHDRAWAL_TIME = "last_withdrawal_time"
        private const val PREF_KEY_YEARLY_COUNT_RESET_TIME = "yearly_count_reset_time"
        private const val PREF_KEY_PHONE_NUMBERS = "withdrawn_phone_numbers"
        private const val MAX_WITHDRAWALS_PER_YEAR = 3
        private const val REACTIVATION_DELAY_HOURS = 24
    }

    private fun performAccountDeletion() {
        // 탈퇴 제한 사항 확인
        val currentTime = System.currentTimeMillis()
        val sharedPrefs = requireActivity().getSharedPreferences(
            "MembershipRestrictionPrefs",
            Context.MODE_PRIVATE
        )

        // 고유 기기 ID 가져오기
        var deviceId = sharedPrefs.getString(PREF_KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(
                requireActivity().contentResolver,
                Settings.Secure.ANDROID_ID
            )
            sharedPrefs.edit().putString(PREF_KEY_DEVICE_ID, deviceId).apply()
        }

        // 1. 연간 탈퇴 횟수 제한 검사
        var yearlyWithdrawalCount = sharedPrefs.getInt(PREF_KEY_WITHDRAWAL_COUNT, 0)
        val yearlyResetTime = sharedPrefs.getLong(PREF_KEY_YEARLY_COUNT_RESET_TIME, 0)

        // 연간 카운터 리셋해야 하는지 확인
        if (yearlyResetTime > 0 && currentTime - yearlyResetTime > 365 * 24 * 60 * 60 * 1000L) {
            yearlyWithdrawalCount = 0
            sharedPrefs.edit().putLong(PREF_KEY_YEARLY_COUNT_RESET_TIME, currentTime).apply()
        } else if (yearlyResetTime == 0L) {
            // 최초 설정
            sharedPrefs.edit().putLong(PREF_KEY_YEARLY_COUNT_RESET_TIME, currentTime).apply()
        }

        // 탈퇴는 횟수 제한 없이 가능
        // yearlyWithdrawalCount는 로그인 화면에서 재가입 제한을 위해서만 확인함

        // 현재 회원 폰번호 가져오기
        val currentMemberInfo = PreferenceUtil.getMemberInfo()
        val phoneNumber = currentMemberInfo?.phoneNumber

        // 회원탈퇴 진행 - API 호출
        // TODO: 서버에 회원탈퇴 API 호출
        // val apiResult = viewModel.withdrawMember()  // 실제 구현 필요

        // 탈퇴한 폰번호 목록에 추가 (폰번호 변경 시 후 재가입 방지용)
        if (phoneNumber != null) {
            val phoneSet = sharedPrefs.getStringSet(PREF_KEY_PHONE_NUMBERS, mutableSetOf<String>())
                ?: mutableSetOf()
            val updatedPhoneSet = phoneSet.toMutableSet()
            updatedPhoneSet.add(phoneNumber)
            sharedPrefs.edit().putStringSet(PREF_KEY_PHONE_NUMBERS, updatedPhoneSet).apply()
        }

        // 탈퇴 카운트 증가 및 탈퇴 시간 저장
        sharedPrefs.edit().apply {
            putInt(PREF_KEY_WITHDRAWAL_COUNT, yearlyWithdrawalCount + 1)
            putLong(PREF_KEY_LAST_WITHDRAWAL_TIME, currentTime)
        }.apply()

        // 로컬 저장소 내 모든 회원 정보 삭제 (PreferenceUtil 외에도 개인정보 저장소 모두 삭제)
        // 1. SharedPreferences
        PreferenceUtil.clear()

        // 2. 앱 내부 데이터베이스 삭제 (필요한 경우)
        // val dbHelper = AppDatabase.getInstance(requireContext())
        // dbHelper.clearAllTables()

        // 3. 캐시 삭제
        try {
            requireContext().cacheDir.deleteRecursively()
        } catch (e: Exception) {
            Log.e("MoreMemberInfoFragment", "Cache deletion error: ${e.message}")
        }

        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_withdrawal_message", true)
            putExtra("withdrawal_time", currentTime)  // 탈퇴 시간 전달
        }
        startActivity(intent)
        requireActivity().finish()
    }

    //새로고침
    private fun refreshMemberInfo() {
        // 로그인 상태 확인
        if (viewModel.isAuthenticated) {
            Log.d("MoreMemberInfoFragment", "회원정보 API 조회")
            viewModel.loadMemberInfo()
        }
    }

    override fun onResume() {
        super.onResume()

        // 화면으로 돌아올 때마다 회원정보 새로고침
        refreshMemberInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.stip.stip.more.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.stip.stip.MainActivity
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreSecurityBinding
import com.stip.stip.more.api.Blockoverseaslogin
import com.stip.stip.more.api.IpAddressService
import com.stip.stip.more.api.IpInfoResponse
import com.stip.stip.more.api.OverseasLoginBlockRequest
import com.stip.stip.more.api.OverseasLoginBlockResponse
import com.stip.stip.signup.customview.LoadingDialog
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MoreSecurityFragment : Fragment() {

    private var _binding: FragmentMoreSecurityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://backend.stipvelation.com/") // IP 주소로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(Blockoverseaslogin::class.java)
    }
    
    // IP 정보를 가져오기 위한 Retrofit 인스턴스
    private val ipRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://ipapi.co/") // IP 정보 서비스 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val ipService by lazy {
        ipRetrofit.create(IpAddressService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateHeaderTitle(getString(R.string.title_security_auth))
        viewModel.enableBackNavigation {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        initializeUI()
    }
    
    override fun onResume() {
        super.onResume()
        // 화면에 재진입할 때마다 생체인증 상태 갱신
        updateBiometricSwitchState()
    }
    
    private fun updateBiometricSwitchState() {
        val sharedPrefBio = requireContext().getSharedPreferences("security_pref", Context.MODE_PRIVATE)
        val isBiometricEnabled = sharedPrefBio.getBoolean("biometric_enabled", false)
        binding.switchBiometricAuth.isEnabled = isBiometricEnabled
        binding.switchBiometricAuth.isChecked = isBiometricEnabled
    }
    
    private fun initializeUI() {
        val sharedPref = requireContext().getSharedPreferences("security_pref", Context.MODE_PRIVATE)
        val isBlocked = sharedPref.getBoolean("overseas_login_blocked", false)
        binding.switchOverseasLoginBlock.isChecked = isBlocked

        // 해외 로그인 차단 박스 클릭 이벤트 처리
        binding.itemOverseasLoginBlock.setOnClickListener {
            // 현재는 차단되어 있으면 해제, 차단되어 있지 않으면 차단으로 상태 변경
            val newBlockState = !binding.switchOverseasLoginBlock.isChecked
            binding.switchOverseasLoginBlock.isChecked = newBlockState
            sharedPref.edit().putBoolean("overseas_login_blocked", newBlockState).apply()
            
            if (newBlockState) {
                // 현재 기기 언어 설정 가져오기
                val deviceLocale = resources.configuration.locales[0].language
                
                // 영어일 경우에는 IP 주소를 사용하여 현재 국가 확인
                if (deviceLocale == "en") {
                    // 사용자 IP 주소 가져오기
                    getIpAddressAndSetRestriction(newBlockState)
                } else {
                    // 한국어, 일본어, 중국어의 경우 해당 언어 국가에서만 사용 가능하도록 설정
                    sendOverseasLoginBlockRequest(newBlockState, deviceLocale, null)
                }
            } else {
                // 차단을 해제하는 경우 - 어디서든 로그인 가능하게 설정
                sendOverseasLoginBlockRequest(false, "all", null)
                Toast.makeText(requireContext(), "해외 로그인 차단이 해제되었습니다. 어디서든 로그인이 가능합니다.", Toast.LENGTH_LONG).show()
            }
        }

        // 해외 로그인 차단 스위치 이벤트 처리
        binding.switchOverseasLoginBlock.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchOverseasLoginBlock.isPressed) { // 스위치 자체를 클릭했을 때만 처리
                sharedPref.edit().putBoolean("overseas_login_blocked", isChecked).apply()
                
                if (isChecked) {
                    // 현재 기기 언어 설정 가져오기
                    val deviceLocale = resources.configuration.locales[0].language
                    
                    // 영어일 경우에는 IP 주소를 사용하여 현재 국가 확인
                    if (deviceLocale == "en") {
                        // 사용자 IP 주소 가져오기
                        getIpAddressAndSetRestriction(isChecked)
                    } else {
                        // 한국어, 일본어, 중국어의 경우 해당 언어 국가에서만 사용 가능하도록 설정
                        sendOverseasLoginBlockRequest(isChecked, deviceLocale, null)
                    }
                } else {
                    // 차단을 해제하는 경우 - 어디서든 로그인 가능하게 설정
                    sendOverseasLoginBlockRequest(false, "all", null)
                    Toast.makeText(requireContext(), "해외 로그인 차단이 해제되었습니다. 어디서든 로그인이 가능합니다.", Toast.LENGTH_LONG).show()
                }
            }
        }

        // PIN 비밀번호 변경 클릭 이벤트 처리
        binding.itemPinChange.setOnClickListener {
            // PIN 번호 입력 화면으로 바로 이동
            val intent = android.content.Intent(requireActivity(), com.stip.stip.more.activity.PinVerificationActivity::class.java)
            // PIN 변경 목적임을 전달
            intent.putExtra("is_pin_change", true)
            startActivity(intent)
        }
        
        binding.itemLoginHistory.setOnClickListener {
            viewModel.updateHeaderTitle(getString(R.string.login_history_title_manage))
            (activity as? MainActivity)?.navigateToLoginHistory()
        }
        
        // 생체인증 설정 박스 클릭 이벤트 처리
        binding.itemBiometricAuth.setOnClickListener {
            // PIN 번호 확인 화면으로 이동
            val intent = android.content.Intent(requireActivity(), com.stip.stip.more.activity.PinVerificationActivity::class.java)
            // 생체인증 설정 목적임을 전달
            intent.putExtra("is_pin_change", false)
            intent.putExtra("is_biometric_setup", true)
            startActivity(intent)
        }
        
        // 생체인증 스위치 초기 상태 설정
        updateBiometricSwitchState()
        
        // 생체인증 스위치 상태 변경 이벤트
        binding.switchBiometricAuth.setOnCheckedChangeListener { _, isChecked ->
            val sharedPrefBio = requireContext().getSharedPreferences("security_pref", Context.MODE_PRIVATE)
            if (binding.switchBiometricAuth.isEnabled) {
                sharedPrefBio.edit().putBoolean("biometric_enabled", isChecked).apply()
                // 실제 서비스에서는 서버에 생체인증 사용 여부 전송 추가 필요
                
                // 생체인증 사용 여부에 따라 메시지 표시
                if (isChecked) {
                    Toast.makeText(requireContext(), "생체인증이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "생체인증이 비활성화되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * IP 주소를 가져와서 국가 제한을 설정하는 함수
     * 영어 사용자의 경우 해당 IP의 국가에서만 로그인 가능하도록 설정
     */
    private fun getIpAddressAndSetRestriction(block: Boolean) {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()
        
        // IP 정보 가져오기
        ipService.getIpInfo().enqueue(object : Callback<IpInfoResponse> {
            override fun onResponse(call: Call<IpInfoResponse>, response: Response<IpInfoResponse>) {
                loadingDialog.dismiss()
                
                if (response.isSuccessful) {
                    val ipInfo = response.body()
                    val userIp = ipInfo?.ip ?: ""
                    val countryCode = ipInfo?.country_code ?: ""
                    
                    Log.d("MoreSecurity", "사용자 IP: $userIp, 국가 코드: $countryCode")
                    
                    // 영어 사용자의 경우 IP 정보를 활용하여 해당 국가에서만 로그인 제한
                    sendOverseasLoginBlockRequest(block, "en", userIp)
                    
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "국가 확인 완료: $countryCode \n현재 국가에서만 로그인이 가능합니다.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e("MoreSecurity", "IP 정보 가져오기 실패: ${response.errorBody()?.string()}")
                    
                    // IP 정보 가져오기 실패 시 기본 언어 재할당
                    sendOverseasLoginBlockRequest(block, "en", null)
                    
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "IP 정보를 가져올 수 없습니다. 영어 설정 기기에는 해외 로그인 제한이 적용됩니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<IpInfoResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("MoreSecurity", "IP 정보 요청 실패", t)
                
                // 네트워크 오류 시 기본 언어 재할당
                sendOverseasLoginBlockRequest(block, "en", null)
                
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "네트워크 오류: IP 정보를 가져올 수 없습니다. 영어 설정 기기에는 해외 로그인 제한이 적용됩니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    /**
     * 해외 로그인 차단 요청을 서버에 전송하는 함수
     * @param block 차단 여부 (true: 차단, false: 해제)
     * @param deviceLocale 현재 기기 언어 설정
     * @param userIpAddress 사용자 IP 주소 (영어 사용자의 경우에만 사용)
     */
    private fun sendOverseasLoginBlockRequest(block: Boolean, deviceLocale: String, userIpAddress: String?) {
        val request = OverseasLoginBlockRequest(
            block = block,
            deviceLocale = deviceLocale,
            restrictToCountryByLanguage = true,
            userIpAddress = userIpAddress
        )
        
        val call = apiService.setOverseasLoginBlock(request)
        call.enqueue(object : Callback<OverseasLoginBlockResponse> {
            override fun onResponse(
                call: Call<OverseasLoginBlockResponse>,
                response: Response<OverseasLoginBlockResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("MoreSecurity", "해외 로그인 차단 설정 완료: ${response.body()?.message}")
                    
                    // 응답에 따라 사용자에게 토스트 메시지 표시
                    activity?.runOnUiThread {
                        if (block) {
                            // 언어별 메시지 제공
                            val message = when (deviceLocale) {
                                "ko" -> "해외 로그인 차단이 활성화되었습니다. 한국어 설정 기기에서만 로그인이 가능합니다."
                                "ja" -> "해외 로그인 차단이 활성화되었습니다. 일본어 설정 기기에서만 로그인이 가능합니다."
                                "zh" -> "해외 로그인 차단이 활성화되었습니다. 중국어 설정 기기에서만 로그인이 가능합니다."
                                "en" -> "해외 로그인 차단이 활성화되었습니다. 현재 국가에서만 로그인이 가능합니다."
                                else -> "해외 로그인 차단이 활성화되었습니다."
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        } else if (deviceLocale != "all") { // "all"인 경우는 이미 타임에서 메시지를 표시했으므로 중복 표시하지 않음
                            Toast.makeText(requireContext(), "해외 로그인 차단이 비활성화되었습니다.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.e("MoreSecurity", "해외 로그인 차단 설정 실패: ${response.errorBody()?.string()}")
                    
                    // 오류 발생 시 사용자에게 토스트 메시지 표시
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "서버 오류가 발생했습니다. 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        
                        // UI 상태 되돌리기 (서버 오류시 스위치 상태 반전)
                        binding.switchOverseasLoginBlock.isChecked = !block
                        val sharedPref = requireContext().getSharedPreferences("security_pref", Context.MODE_PRIVATE)
                        sharedPref.edit().putBoolean("overseas_login_blocked", !block).apply()
                    }
                }
            }

            override fun onFailure(call: Call<OverseasLoginBlockResponse>, t: Throwable) {
                Log.e("MoreSecurity", "해외 로그인 차단 요청 실패", t)
                
                // 네트워크 오류 시 사용자에게 토스트 메시지 표시
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    
                    // UI 상태 되돌리기
                    binding.switchOverseasLoginBlock.isChecked = !block
                    val sharedPref = requireContext().getSharedPreferences("security_pref", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("overseas_login_blocked", !block).apply()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

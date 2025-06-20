package com.stip.stip.signup.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import com.stip.stip.MainActivity
import com.stip.stip.R
import com.stip.stip.databinding.ActivityLoginPinNumberBinding
import com.stip.stip.signup.Constants
import com.stip.stip.signup.base.BaseActivity
import com.stip.stip.signup.biometric.BiometricAuthHelper
import com.stip.stip.signup.customview.BiometricAuthDialog
import com.stip.stip.signup.customview.CustomContentDialog
import com.stip.stip.signup.keypad.KeypadAdapter
import com.stip.stip.signup.keypad.KeypadItem
import com.stip.stip.signup.keypad.KeypadType
import com.stip.stip.signup.model.RequestAuthLogin
import com.stip.stip.model.MemberInfo
import com.stip.stip.signup.pin.PinAdapter
import com.stip.stip.signup.signup.SignUpActivity
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginPinNumberActivity : BaseActivity<ActivityLoginPinNumberBinding, LoginViewModel>(), BiometricAuthHelper.AuthCallback {

    companion object {
        fun startLoginPinNumberActivity(
            activity: Activity,
        ) {
            val intent = Intent(activity, LoginPinNumberActivity::class.java).apply {
            }
            activity.startActivity(intent)
        }

        fun startLoginPinNumberActivityFinish(
            activity: Activity,
        ) {
            val intent = Intent(activity, LoginPinNumberActivity::class.java).apply {
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_login_pin_number

    override val viewModel: LoginViewModel by viewModels()

    private lateinit var pinAdapter: PinAdapter
    private lateinit var keypadAdapter: KeypadAdapter
    private val pinInput = StringBuilder()
    
    // 생체인증 관련
    private lateinit var biometricAuthHelper: BiometricAuthHelper
    private val TAG = "LoginPinNumberActivity"

    // PIN 번호 시도 횟수
    private var pinAttemptCount = 0
    private val MAX_PIN_ATTEMPTS = 5

    override fun initStartView() {
        val pinTestList = MutableList(6) { false }
        pinAdapter = PinAdapter(pinTestList)
        binding.rvPassword.adapter = pinAdapter
        
        // 생체인증 헬퍼 초기화
        biometricAuthHelper = BiometricAuthHelper(this, this)
        
        // 휴대폰 인증 검사 생략하고 바로 키패드 표시
        setKeyPad()
    }

    override fun initDataBinding() {
        with(viewModel) {
            isLoading.observe(this@LoginPinNumberActivity) {
                if (it) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }

            pinNumberBasicSettingLiveData.observe(this@LoginPinNumberActivity) {
                if (it) {
                    PreferenceUtil.putString(
                        Constants.PREF_KEY_BASIC_LOGIN_TYPE,
                        Constants.BASIC_LOGIN_TYPE_PIN_NUMBER
                    )
                }
            }

            authLoginLiveData.observe(this@LoginPinNumberActivity) {
                PreferenceUtil.putString(Constants.PREF_KEY_PIN_VALUE, pinInput.toString())
                PreferenceUtil.putString(Constants.PREF_KEY_AUTH_TOKEN_VALUE, it.accessToken)
                MainActivity.startMainActivity(this@LoginPinNumberActivity)
                finish()
            }

            errorLiveData.observe(this@LoginPinNumberActivity) {
                val errorMessage: String = when (it) {
                    Constants.NETWORK_DUPLICATE_ERROR_CODE -> getString(R.string.error_duplicate_409)
                    Constants.NETWORK_SERVER_ERROR_CODE -> getString(R.string.error_admin_500)
                    Constants.NETWORK_LOGIN_FAIL_CODE -> getString(R.string.error_login_fail_888)
                    else -> getString(R.string.error_network_999)
                }

                CustomContentDialog(
                    binding.root.context
                ) {
                }.setText(
                    getString(R.string.dialog_bank_guide_title),
                    errorMessage,
                    "",
                    getString(R.string.common_confirm)
                )
            }
        }
    }

    override fun initAfterBinding() {
        setOnClick(binding.ivBack) {
            finish()
        }

        setOnClick(binding.tvForgetPassword) {
            // isPinForgot을 true로 설정하여 휴대폰 인증 후 PIN번호 재설정할 수 있도록 함
            SignUpActivity.startSignUpActivityPinNumberChange(this@LoginPinNumberActivity, true, true)
        }

        // 생체인증으로 로그인 버튼 클릭 리스너 설정 - 현재 화면에서 바로 생체인증 처리
        setOnClick(binding.btnBiometricLogin) {
            // 바로 생체인증 다이얼로그 실행
            biometricAuthHelper.showBiometricPrompt()
        }
    }

    private fun setKeyPad() {
        val numberItems = (0..9).map { KeypadItem(it.toString(), KeypadType.NUMBER) }.shuffled()
        val fixedItems = listOf(
            KeypadItem(getString(R.string.common_re_order), KeypadType.SHUFFLE),
            numberItems.first(),
            KeypadItem("", KeypadType.DELETE, R.drawable.ic_del_white_31dp)
        )
        val keypadItemList = (numberItems.drop(1) + fixedItems).toMutableList()
        keypadAdapter = KeypadAdapter(
            keypadItemList
        ) { item ->
            when (item.type) {
                KeypadType.NUMBER -> {
                    if (pinInput.length < 6) {
                        pinInput.append(item.value)
                        pinAdapter.updatePinCount(pinInput.length)

                        if (pinInput.length == 6) {
                            checkPinAndLoginIfMatch()
                        }
                    }
                }

                KeypadType.DELETE -> {
                    if (pinInput.isNotEmpty()) {
                        pinInput.deleteAt(pinInput.lastIndex)
                        pinAdapter.updatePinCount(pinInput.length)
                    }
                }
                KeypadType.SHUFFLE -> {
                    pinInput.clear()
                    pinAdapter.updatePinCount(0)
                    setKeyPad()
                }
            }
        }
        binding.rvNumber.adapter = keypadAdapter
    }
    
    // 생체인증 콜백 메서드 구현
    override fun onAuthSuccess() {
        // 인증 성공 시 바로 로그인 처리
        Log.d(TAG, "생체인증 성공, 로그인 시도")
        
        // 저장된 PIN과 DI 값으로 로그인 요청
        viewModel.requestPostAuthLogin(
            RequestAuthLogin(
                di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, ""),
                pin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE, "")
            )
        )
    }

    override fun onAuthFailed() {
        // 인증 일치하지 않음
        Log.e(TAG, "생체인증 불일치")
        
        CustomContentDialog(
            binding.root.context
        ) {
        }.setText(
            getString(R.string.dialog_bank_guide_title),
            "생체인증에 실패했습니다. 다시 시도하거나 PIN 번호로 로그인해주세요.",
            "",
            getString(R.string.common_confirm)
        )
    }

    override fun onAuthError(errorMsg: String) {
        // 인증 실패 (오류)
        Log.e(TAG, "생체인증 오류: $errorMsg")
        
        CustomContentDialog(
            binding.root.context
        ) {
        }.setText(
            getString(R.string.dialog_bank_guide_title),
            "생체인증 과정에서 오류가 발생했습니다. PIN 번호로 로그인해주세요.",
            "",
            getString(R.string.common_confirm)
        )
    }

    private fun checkPinAndLoginIfMatch() {
        if (pinInput.length == 6) {
            val savedPin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE)
            val enteredPin = pinInput.toString()
            val di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, "")

            // 핀 검증 전 입력 버퍼 미리 초기화 (중요: 이후 검증 결과에 관계없이 입력은 항상 초기화)
            val currentPinInput = enteredPin // 임시 저장
            pinInput.setLength(0) // 입력 버퍼 초기화
            pinAdapter.updatePinCount(0) // 화면 PIN 점들 초기화

            // 최대 시도 횟수 확인
            if (pinAttemptCount >= MAX_PIN_ATTEMPTS) {
                CustomContentDialog(binding.root.context) {
                    finish()
                }.setText(
                    getString(R.string.dialog_bank_guide_title),
                    "PIN 번호를 5회 이상 잘못 입력하셨습니다. 휴대폰 인증을 통해 다시 시도해주세요.",
                    "",
                    getString(R.string.common_confirm)
                )
                return
            }

            // 테스트용 PIN "123456" 추가 - 모든 PIN 검증 과정 생략하고 바로 로그인
            if (currentPinInput == "123456") {
                android.util.Log.d("LoginPin", "테스트 PIN 입력 감지 - 전체 액세스 허용 로그인 진행")
                pinAttemptCount = 0
                
                // 모든 필요한 인증 관련 정보와 토큰 저장
                val testToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTYiLCJkaXNwbGF5X25hbWUiOiJUZXN0VXNlciIsImlhdCI6MTY1MDAwMDAwMH0.8PA01KGmpsKtUJMdALBErk8qv2QAjBj2J-dzfK_iUYA" 
                val testDi = "test_di_123456"
                val testName = "Test User"
                val testPhone = "010-1234-5678"
                
                // 핀 번호 저장
                PreferenceUtil.putString(Constants.PREF_KEY_PIN_VALUE, currentPinInput)
                
                // 인증 토큰 저장 (표준 JWT 베어러 토큰 형식으로 저장)
                // PREF_KEY_AUTH_TOKEN_VALUE에 저장 및 두 가지 하위 호환성을 위해 추가 저장
                PreferenceUtil.putString(Constants.PREF_KEY_AUTH_TOKEN_VALUE, testToken)
                
                // MainActivity에서 사용하는 인증 토큰 키에도 저장 (KEY_JWT_TOKEN)
                PreferenceUtil.saveToken(testToken)
                
                // DI 값 저장 (중요: 모든 API 호출에 필요)
                PreferenceUtil.putString(Constants.PREF_KEY_DI_VALUE, testDi)
                PreferenceUtil.saveMemberDi(testDi)
                
                // 그 외 추가 필요한 정보 저장
                PreferenceUtil.putString("user_name", testName)
                PreferenceUtil.putString("user_phone", "01012345678")
                PreferenceUtil.putString("user_email", "test@example.com")

                // MemberInfo 데이터 클래스에 맞게 새로운 미리 정의된 회원 정보 생성
                val memberInfo = MemberInfo(
                    name = testName,
                    englishFirstName = "Test",
                    englishLastName = "User",
                    telecomProvider = "SKT",
                    phoneNumber = "01012345678",
                    birthdate = "1990-01-01",
                    email = "test@example.com",
                    bankCode = "004",
                    accountNumber = "123456789",
                    address = "서울특별시 강남구",
                    addressDetail = "테헤란로 123",
                    postalCode = "06134",
                    isDirectAccount = true,
                    usagePurpose = "INVESTMENT",
                    sourceOfFunds = "SALARY", 
                    job = "개발자",
                    id = "12345"
                )
                
                // PreferenceUtil의 회원 정보 저장 메소드를 사용하여 저장
                // 이것은 MainActivity와 모든 모듈에서 사용하는 회원 정보 검색 메소드와 일치
                PreferenceUtil.saveMemberInfo(memberInfo)
                
                android.util.Log.d("LoginPin", "테스트 로그인 완료 - 모든 기능 액세스 활성화 - 메인 화면으로 이동")
                
                // ipasset과 morefragment 기능을 위해 MainActivity에 로그인 완료 플래그 전달
                // MainActivity에서 이 플래그를 확인하고 UI 상태를 갱신함
                val mainIntent = Intent(this@LoginPinNumberActivity, com.stip.stip.MainActivity::class.java)
                mainIntent.putExtra("FROM_LOGIN", true)
                startActivity(mainIntent)
                finish() // 로그인 화면 종료
                return
            }
            
            if (savedPin.isBlank()) {
                // 저장된 PIN 번호 없음 - API 호출
                viewModel.requestPostAuthLogin(
                    RequestAuthLogin(
                        di = di,
                        pin = currentPinInput
                    )
                )
            } else {
                if (currentPinInput == savedPin) {
                    // PIN 번호 일치 - 성공
                    pinAttemptCount = 0  // 시도 횟수 초기화
                    
                    // 로그인 API 호출
                    viewModel.requestPostAuthLogin(
                        RequestAuthLogin(
                            di = di,
                            pin = currentPinInput
                        )
                    )
                } else {
                    // PIN 불일치 - 시도 횟수 증가 및 오류 메시지 표시
                    pinAttemptCount++
                    
                    // 팝업 메시지
                    // 경고 메시지 작성 - 시도 횟수를 명시적으로 포매팅
                    val errorMessage = "로그인 실패: PIN 번호가 일치하지 않습니다. (" + pinAttemptCount + "/" + MAX_PIN_ATTEMPTS + ")"
                    android.util.Log.d("LoginPin", "Error message: $errorMessage")

                    CustomContentDialog(binding.root.context) {
                        // 닫기 버튼 클릭 시 아무 작업 없음 (이미 PIN 입력은 초기화됨)
                    }.setText(
                        getString(R.string.dialog_bank_guide_title),
                        errorMessage,
                        "",
                        getString(R.string.common_confirm)
                    )
                }
            }
        }
    }
}
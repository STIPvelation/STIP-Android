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

            // DI 값이 비어있어도 로그인 기능 허용 (휴대폰 인증 건너뛰기)

            // 최대 시도 횟수 확인
            if (pinAttemptCount >= MAX_PIN_ATTEMPTS) {
                // 5회 이상 시도했으뭴 오류 표시
                CustomContentDialog(
                    binding.root.context
                ) {
                    // PIN 번호 잘못입력 한계 도달 시 휴대폰 인증 화면으로 이동
                    finish()
                }.setText(
                    getString(R.string.dialog_bank_guide_title),
                    "PIN 번호를 5회 이상 잘못 입력하셨습니다. 휴대폰 인증을 통해 다시 시도해주세요.",
                    "",
                    getString(R.string.common_confirm)
                )
                return
            }

            if (savedPin.isBlank()) {
                // 저장된 PIN 번호 없음
                // API 호출
                viewModel.requestPostAuthLogin(
                    RequestAuthLogin(
                        di = di,
                        pin = enteredPin
                    )
                )
            } else {
                if (enteredPin == savedPin) {
                    // PIN 번호 일치 - 성공
                    // 시도 횟수 초기화
                    pinAttemptCount = 0

                    // 로그인 API 호출
                    viewModel.requestPostAuthLogin(
                        RequestAuthLogin(
                            di = di,
                            pin = enteredPin
                        )
                    )
                } else {
                    // PIN 번호 불일치 - 시도 횟수 증가
                    pinAttemptCount++

                    val attemptCount = pinAttemptCount
                    if (attemptCount < MAX_PIN_ATTEMPTS) {
                        // 시도 횟수를 팝업으로 표시 (1/5, 2/5 등)
                        CustomContentDialog(
                            binding.root.context
                        ) {
                            // PIN 번호 다시 입력할 수 있도록 아무 동작 없음
                        }.setText(
                            getString(R.string.dialog_bank_guide_title),
                            "PIN 번호가 일치하지 않습니다.\n(${attemptCount}/${MAX_PIN_ATTEMPTS})",
                            "",
                            getString(R.string.common_confirm)
                        )
                        android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생 - 시도 횟수: $attemptCount/$MAX_PIN_ATTEMPTS")
                    } else {
                        android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생!!!투투")
                        // 마지막 시도 실패
                        CustomContentDialog(
                            binding.root.context
                        ) {
                            // PIN 번호 잘못입력 한계 도달 시 휴대폰 인증 화면으로 이동
                            finish()
                        }.setText(
                            getString(R.string.dialog_bank_guide_title),
                            "PIN 번호를 5회 이상 잘못 입력하셨습니다. 휴대폰 인증을 통해 다시 시도해주세요.",
                            "",
                            getString(R.string.common_confirm)
                        )
                    }
                    pinInput.clear()
                    pinAdapter.updatePinCount(0)
                }
            }
        }
    }
}
package com.stip.stip.signup.login

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import com.stip.stip.MainActivity
import com.stip.stip.R
import com.stip.stip.databinding.ActivityLoginPinNumberBinding
import com.stip.stip.signup.Constants
import com.stip.stip.signup.base.BaseActivity
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
class LoginPinNumberActivity : BaseActivity<ActivityLoginPinNumberBinding, LoginViewModel>() {

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

    // PIN 번호 시도 횟수
    private var pinAttemptCount = 0
    private val MAX_PIN_ATTEMPTS = 5

    override fun initStartView() {
        val pinTestList = MutableList(6) { false }
        pinAdapter = PinAdapter(pinTestList)
        binding.rvPassword.adapter = pinAdapter
        val di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, "")

        if (di.isBlank()) {
            // DI가 없으면 다이얼로그 표시 후 인증 화면으로 이동
            CustomContentDialog(
                binding.root.context
            ) {
                // 확인 버튼 누르면 인증 화면으로 이동
                try {
                    val intent = Intent(
                        this,
                        SignUpActivity::class.java
                    ) // SignUpAuthNiceFragment를 포함하는 Activity
                    intent.putExtra("SHOW_SIGN_UP_AUTH_NICE", true)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.setText(
                getString(R.string.dialog_bank_guide_title),
                "휴대폰 인증이 필요합니다. 휴대폰 인증 후 다시 시도해주세요.",
                "",
                getString(R.string.common_confirm)
            )

            return
        }
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
            SignUpActivity.startSignUpActivityPinNumberChange(this@LoginPinNumberActivity, true)
        }

        setOnClick(binding.checkboxBasicLogin) {
            viewModel.setPinNumberBasicSetting(binding.checkboxBasicLogin.isChecked)
        }

        setOnClick(binding.tvAnotherLoginMethod) {
            BiometricAuthDialog(
                this@LoginPinNumberActivity, {
                    // 생체 인증 클릭
                    // 생체 인증 화면 이동
                    LoginBiometricAuthActivity.startLoginBiometricAuthActivityFinish(this)
                }, {
                    // PIN 번호 클릭
                    android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생!!!식식")
                }
            ).show()
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

                KeypadType.SHUFFLE -> keypadAdapter.shuffleNumbers()
                KeypadType.DELETE -> {
                    if (pinInput.isNotEmpty()) {
                        pinInput.deleteCharAt(pinInput.length - 1)
                        pinAdapter.updatePinCount(pinInput.length)
                    }
                }
            }
        }
        binding.rvNumber.adapter = keypadAdapter
    }

    private fun checkPinAndLoginIfMatch() {
        if (pinInput.length == 6) {
            val savedPin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE)
            val enteredPin = pinInput.toString()
            val di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, "")

            // DI가 비어있는지 확인
            if (di.isBlank()) {
                // DI가 없으면 오류 표시
                CustomContentDialog(
                    binding.root.context
                ) {}.setText(
                    getString(R.string.dialog_bank_guide_title),
                    "휴대폰 인증이 필요합니다. 휴대폰 인증 후 다시 시도해주세요.",
                    "",
                    getString(R.string.common_confirm)
                )
                pinInput.clear()
                pinAdapter.updatePinCount(0)
                return
            }

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

                    val remainingAttempts = MAX_PIN_ATTEMPTS - pinAttemptCount
                    if (remainingAttempts > 0) {
                        // 남은 시도 횟수 표시
                        showToast("PIN 번호가 일치하지 않습니다. 남은 시도: ${remainingAttempts}회")
                        android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생!!!원원")
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
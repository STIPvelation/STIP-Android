package com.stip.stip.signup.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import com.stip.stip.MainActivity
import com.stip.stip.R
import com.stip.stip.databinding.ActivityLoginBiometricAuthBinding
import com.stip.stip.signup.Constants
import com.stip.stip.signup.base.BaseActivity
import com.stip.stip.signup.biometric.BiometricAuthHelper
import com.stip.stip.signup.customview.BiometricAuthDialog
import com.stip.stip.signup.customview.CustomContentDialog
import com.stip.stip.signup.model.RequestAuthLogin
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginBiometricAuthActivity : BaseActivity<ActivityLoginBiometricAuthBinding, LoginViewModel>(), BiometricAuthHelper.AuthCallback {

    companion object {
        fun startLoginBiometricAuthActivity(
            activity: Activity,
        ) {
            val intent = Intent(activity, LoginBiometricAuthActivity::class.java).apply {
            }
            activity.startActivity(intent)
        }

        fun startLoginBiometricAuthActivityFinish(
            activity: Activity,
        ) {
            val intent = Intent(activity, LoginBiometricAuthActivity::class.java).apply {
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_login_biometric_auth

    override val viewModel: LoginViewModel by viewModels()

    private val TAG = this::class.qualifiedName
    private lateinit var biometricAuthHelper: BiometricAuthHelper


    override fun initStartView() {
        biometricAuthHelper = BiometricAuthHelper(this, this)
    }

    override fun initDataBinding() {
        with(viewModel) {
            isLoading.observe(this@LoginBiometricAuthActivity) {
                if (it) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }

            biometricAuthBasicSettingLiveData.observe(this@LoginBiometricAuthActivity) {
                if (it) {
                    PreferenceUtil.putString(
                        Constants.PREF_KEY_BASIC_LOGIN_TYPE,
                        Constants.BASIC_LOGIN_TYPE_BIOMETRIC_AUTH
                    )
                }
            }

            authLoginLiveData.observe(this@LoginBiometricAuthActivity) {
                PreferenceUtil.putString(Constants.PREF_KEY_AUTH_TOKEN_VALUE, it.accessToken)
                MainActivity.startMainActivity(this@LoginBiometricAuthActivity)
                finish()
            }

            errorLiveData.observe(this@LoginBiometricAuthActivity) {
                val errorMessage: String = when (it) {
                    Constants.NETWORK_DUPLICATE_ERROR_CODE -> getString(R.string.error_duplicate_409)
                    Constants.NETWORK_SERVER_ERROR_CODE -> getString(R.string.error_admin_500)
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

        setOnClick(binding.checkboxBasicLogin) {
            viewModel.setBiometricAuthBasicSetting(binding.checkboxBasicLogin.isChecked)
        }

        setOnClick(binding.ivBiometricAuth) {
            biometricAuthHelper.showBiometricPrompt()
        }

        setOnClick(binding.tvAnotherLoginMethod) {
            BiometricAuthDialog(
                this@LoginBiometricAuthActivity, {
                    // 생체 인증 클릭
                }, {
                    // PIN 번호 클릭
                    // Pin 번호 화면 이동
                    LoginPinNumberActivity.startLoginPinNumberActivityFinish(this)
                }
            ).show()
        }
    }

    override fun onAuthSuccess() {
        // 인증 성공
        viewModel.requestPostAuthLogin(
            RequestAuthLogin(
                di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, ""),
                pin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE, "")
            )
        )
    }

    override fun onAuthFailed() {
        // 인증 일치하지 않음
        Log.e(TAG, "지문 일치 하지 않음")
    }

    override fun onAuthError(errorMsg: String) {
        // 인증 실패
        Log.d(TAG, "지문 error = $errorMsg")
    }
}
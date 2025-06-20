package com.stip.stip.signup.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.stip.stip.R
import com.stip.stip.signup.base.BaseActivity
import com.stip.stip.databinding.ActivityLoginBinding
import com.stip.stip.MainActivity
import com.stip.stip.signup.signup.SignUpStartActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LoginActivity: BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    companion object {
        private const val PREFS_NAME = "MembershipRestrictionPrefs"
        private const val PREF_KEY_DEVICE_ID = "device_unique_id"
        private const val PREF_KEY_WITHDRAWAL_COUNT = "withdrawal_count_yearly"
        private const val PREF_KEY_LAST_WITHDRAWAL_TIME = "last_withdrawal_time"
        private const val PREF_KEY_PHONE_NUMBERS = "withdrawn_phone_numbers"
        private const val MAX_WITHDRAWALS_PER_YEAR = 3
        private const val REACTIVATION_DELAY_HOURS = 24
        
        fun startLoginActivity(
            activity: Activity,
        ) {
            val intent = Intent(activity, LoginActivity::class.java).apply {
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_login

    override val viewModel: LoginViewModel by viewModels()

    override fun initStartView() {
        val language = resources.configuration.locales[0].language
        val titleResId = if (language == "ko") {
            R.drawable.ic_login_title
        } else {
            R.drawable.ic_login_english
        }

        binding.tvTitle.setImageResource(titleResId)
        Log.e("start","세상의")

        // 로그인 이력에 따른 자동 이동 기능 비활성화
        // PIN 번호 화면이 자동으로 나타나지 않도록 기능 제거
        // val basicLoginType = PreferenceUtil.getString(Constants.PREF_KEY_BASIC_LOGIN_TYPE, "")
        // if (basicLoginType.isNotBlank()) {
        //    when (basicLoginType) {
        //        Constants.BASIC_LOGIN_TYPE_PIN_NUMBER -> LoginPinNumberActivity.startLoginPinNumberActivity(this)
        //        Constants.BASIC_LOGIN_TYPE_BIOMETRIC_AUTH -> LoginBiometricAuthActivity.startLoginBiometricAuthActivity(this)
        //    }
        // }
    }

    override fun initDataBinding() {
    }
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 회원탈퇴 후 메시지 표시
        if (intent.getBooleanExtra("show_withdrawal_message", false)) {
            val withdrawalTime = intent.getLongExtra("withdrawal_time", 0L)
            if (withdrawalTime > 0) {
                showWithdrawalMessage(withdrawalTime)
            }
        }
    }

    private fun showWithdrawalMessage(withdrawalTime: Long) {
        // 추가 다이얼로그 표시 없이 전체 기능 유지
    }
    
    /**
     * 회원탈퇴 제한 조건을 확인하는 함수
     * - 24시간 내 재가입 불가
     * - 1년에 3회 이상 탈퇴 불가
     */
    private fun checkWithdrawalRestrictions(): Boolean {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        // 기기 ID 확인
        var deviceId = sharedPrefs.getString(PREF_KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            sharedPrefs.edit().putString(PREF_KEY_DEVICE_ID, deviceId).apply()
        }
        
        // 1. 마지막 탈퇴 시간 확인 (24시간 제한)
        val lastWithdrawalTime = sharedPrefs.getLong(PREF_KEY_LAST_WITHDRAWAL_TIME, 0)
        if (lastWithdrawalTime > 0) {
            val elapsedHours = TimeUnit.MILLISECONDS.toHours(currentTime - lastWithdrawalTime)
            if (elapsedHours < REACTIVATION_DELAY_HOURS) {
                // 24시간 제한 적용 (메시지 없이)
                return false
            }
        }
        
        // 2. 연간 탈퇴 횟수 확인
        val yearlyWithdrawalCount = sharedPrefs.getInt(PREF_KEY_WITHDRAWAL_COUNT, 0)
        if (yearlyWithdrawalCount >= MAX_WITHDRAWALS_PER_YEAR) {
            // 재가입 시도 차단 (메시지 없이)
            return false
        }
        
        return true
    }

    override fun initAfterBinding() {
        setOnClick(binding.tvLookAround) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        setOnClick(binding.btnLogin) {
            /**
             * PIN 번호 입력 화면으로 바로 이동
             */
            LoginPinNumberActivity.startLoginPinNumberActivity(this)
        }

        setOnClick(binding.btnSignup) {
            /**
             * 회원가입 전 탈퇴 제한 조건 확인
             */
            android.util.Log.d("LoginActivity", "회원가입 버튼 클릭됨, 제한 조건 확인")
            if (checkWithdrawalRestrictions()) {
                android.util.Log.d("LoginActivity", "제한 조건 통과, 약관 동의 화면으로 바로 이동")
                // SignUpStartActivity 및 로딩 화면을 건너뛰고 바로 약관 화면(SignUpActivity)으로 이동
                com.stip.stip.signup.signup.SignUpActivity.startSignUpActivity(this)
            } else {
                android.util.Log.d("LoginActivity", "제한 조건 걸리기 때문에 회원가입 불가")
                // 제한 조건에 걸리면 Toast 메시지를 표시하고 회원가입 프로세스 차단
                val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val lastWithdrawalTime = sharedPrefs.getLong(PREF_KEY_LAST_WITHDRAWAL_TIME, 0)
                val yearlyWithdrawalCount = sharedPrefs.getInt(PREF_KEY_WITHDRAWAL_COUNT, 0)
                
                if (lastWithdrawalTime > 0) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(currentTime - lastWithdrawalTime)
                    if (elapsedHours < REACTIVATION_DELAY_HOURS) {
                        Toast.makeText(this, "탈퇴 후 ${REACTIVATION_DELAY_HOURS}시간 동안 회원가입이 불가합니다.", Toast.LENGTH_LONG).show()
                        return@setOnClick
                    }
                }
                
                if (yearlyWithdrawalCount >= MAX_WITHDRAWALS_PER_YEAR) {
                    Toast.makeText(this, "연간 탈퇴 횟수(${MAX_WITHDRAWALS_PER_YEAR}회)를 초과하여 회원가입이 불가합니다.", Toast.LENGTH_LONG).show()
                    return@setOnClick
                }
                
                // 나머지 경우에는 일반적인 오류 메시지 표시
                Toast.makeText(this, "회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

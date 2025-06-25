package com.stip.stip.signup.intro

import android.util.Log
import androidx.activity.viewModels
import com.stip.stip.signup.Constants
import com.stip.stip.signup.Constants.TAG
import com.stip.stip.R
import com.stip.stip.signup.base.BaseActivity
import com.stip.stip.databinding.ActivityIntroBinding
import com.stip.stip.signup.login.LoginActivity
import com.stip.stip.signup.permission.PermissionActivity
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class IntroActivity: BaseActivity<ActivityIntroBinding, IntroViewModel>() {

    override val layoutResource: Int
        get() = R.layout.activity_intro

    override val viewModel: IntroViewModel by viewModels()

    override fun initStartView() {
        Log.e(TAG,"token = ${PreferenceUtil.getString(Constants.PREF_KEY_AUTH_TOKEN_VALUE)}")
        Log.e(TAG,"di = ${PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE)}")
        Log.e(TAG,"pin = ${PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE)}")

        val di = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE)
        val pin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)

            if (di.isNotBlank() && pin.isNotBlank()) {
                LoginActivity.startLoginActivity(this@IntroActivity)
            } else {
                PermissionActivity.startPermissionActivity(this@IntroActivity)
            }
        }
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
    }
}
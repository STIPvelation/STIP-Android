package com.stip.stip.signup.permission

import android.app.Activity
import android.content.Intent
import android.text.Html
import androidx.activity.viewModels
import com.stip.stip.R
import com.stip.stip.signup.base.BaseActivity
import com.stip.stip.databinding.ActivityPermissionBinding
import com.stip.stip.signup.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionActivity: BaseActivity<ActivityPermissionBinding, PermissionViewModel>() {

    companion object {
        fun startPermissionActivity(
            activity: Activity,
        ) {
            val intent = Intent(activity, PermissionActivity::class.java).apply {
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_permission

    override val viewModel: PermissionViewModel by viewModels()

    override fun initStartView() {
        val result = String.format(binding.root.context.getString(R.string.permission_title))
        binding.tvPermissionExplain.text = Html.fromHtml(result)
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
        setOnClick(binding.btnOk) {
            LoginActivity.startLoginActivity(this)
        }
    }
}
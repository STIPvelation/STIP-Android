package com.stip.stip.signup.signup

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import com.stip.stip.R
import com.stip.stip.databinding.ActivityLoginStartBinding
import com.stip.stip.signup.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class SignUpStartActivity : BaseActivity<ActivityLoginStartBinding, SignUpViewModel>() {

    companion object {
        fun startSignUpStartActivity(context: Context) {
            val intent = Intent(context, SignUpStartActivity::class.java)
            context.startActivity(intent)
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_login_start

    override val viewModel: SignUpViewModel by viewModels()

    override fun initStartView() {
        // "STIP" 텍스트 강조
        val htmlText = getString(R.string.login_start_title)
        val spannedText = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableString(spannedText)
        val blueColor = ContextCompat.getColor(this, R.color.main_point)
        spannable.setSpan(ForegroundColorSpan(blueColor), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvTitle.text = spannable

        // Start 버튼 클릭 시 로딩 → 회원가입 화면으로 전환
        binding.btnOk.setOnClickListener {
            showLoadingAndNavigate()
        }

        // 닫기 버튼
        binding.ivClose.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showLoadingAndNavigate() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_loading)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        binding.btnOk.postDelayed({
            dialog.dismiss()
            SignUpActivity.startSignUpActivity(this)
        }, 1500)
    }

    override fun initDataBinding() {
        // 필요 시 LiveData observe 설정
    }

    override fun initAfterBinding() {
        // 필요 시 후처리
    }
}

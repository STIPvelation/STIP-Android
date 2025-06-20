package com.stip.stip.more.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMorePolicyBinding

class MorePolicyFragment : Fragment() {

    private var _binding: FragmentMorePolicyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMorePolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
            binding.itemCurrencyExchange,
            binding.itemTermsInvestmentRisk,
            binding.itemTermsCertificateService,
            binding.itemTermsIdentificationInfo,
            binding.itemTermsPersonalInfoCollectionAuth,
            binding.itemTermsPersonalInfoCollection,
            binding.itemTermsAutoDebit,
            binding.itemTermsExchangeUsage,
            binding.itemTermsYouthProtection,
            binding.itemTermsPrivacyPolicy,
            binding.itemTermsPersonalInfoUse,
            binding.itemTermsEsgPolicy,
            binding.itemTermsStartupPolicy,
            binding.itemTermsAmlPolicy,
            binding.itemTermsDipEvaluation,
            binding.itemTermsFeesPolicy
        )

        items.forEachIndexed { index, itemView ->
            itemView.setOnClickListener {
                showPolicyDialog(index)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateHeaderTitle(getString(R.string.header_policy_terms))
        viewModel.updateNavigationIcon(R.drawable.ic_arrow_return)
        viewModel.updateNavigationClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showPolicyDialog(index: Int) {
        showPolicyDialog(requireContext(), index)
    }

    companion object {
        fun showPolicyDialog(context: Context, index: Int) {
            val titles = context.resources.getStringArray(R.array.policy_terms_title_array)
            val contents = context.resources.getStringArray(R.array.policy_terms_content_array)

            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_policy_confirm, null)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            val titleText = titles.getOrNull(index) ?: "정책 제목 없음"
            val contentText = contents.getOrNull(index) ?: "본문 내용이 없습니다."
            val spannable = SpannableString(contentText)

            val koreanHeaderRegex = Regex("""제(1[0-9]|20|[1-9])조""")
            koreanHeaderRegex.findAll(contentText).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val numberHeaderRegex = Regex("""(?m)^\s*((?:10|[1-9])\.).*""")
            numberHeaderRegex.findAll(contentText).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            dialogView.findViewById<TextView>(R.id.textTitle).text = titleText
            dialogView.findViewById<TextView>(R.id.textContent)
                .setText(spannable, TextView.BufferType.SPANNABLE)

            dialogView.findViewById<Button>(R.id.buttonConfirm).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

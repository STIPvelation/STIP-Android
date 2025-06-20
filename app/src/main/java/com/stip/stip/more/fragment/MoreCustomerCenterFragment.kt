package com.stip.stip.more.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.stip.stip.MainViewModel
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreCustomerCenterBinding

class MoreCustomerCenterFragment : Fragment() {

    private var _binding: FragmentMoreCustomerCenterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreCustomerCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전화 상담 연결
        binding.itemPhoneConsult.setOnClickListener {
            val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:0222384345")
            }
            startActivity(phoneIntent)
        }

        // 카카오톡 문의
        binding.itemKakaoInquiry.setOnClickListener {
            val kakaoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pf.kakao.com/_ahaGn"))
            startActivity(kakaoIntent)
        }

        // 1:1 이메일 문의
        binding.itemOneOnOneInquiry.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@stip.global")
                putExtra(Intent.EXTRA_SUBJECT, "[STIP] 1:1 문의")
            }
            startActivity(Intent.createChooser(emailIntent, "이메일 앱 선택"))
        }

        // 문의 내역 바로가기
        binding.itemInquiryHistory.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stipvelation.com/contact-contact.html"))
            startActivity(browserIntent)
        }

        // 준비중 항목들
        val comingSoonToast = { Toast.makeText(requireContext(), "준비중", Toast.LENGTH_SHORT).show() }
        binding.itemUserGuide.setOnClickListener { comingSoonToast() }
        binding.itemImpersonationReport.setOnClickListener { comingSoonToast() }
        binding.itemUnfairTrade.setOnClickListener { comingSoonToast() }
        binding.itemTransactionStatement.setOnClickListener { comingSoonToast() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateHeaderTitle(getString(R.string.header_customer_center))
        viewModel.enableBackNavigation {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

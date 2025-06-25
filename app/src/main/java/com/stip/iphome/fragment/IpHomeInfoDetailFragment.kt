package com.stip.stip.iphome.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.iphome.adapter.IpHomeInfoDetailAdapter
import com.stip.stip.databinding.FragmentIpHomeInfoDetailBinding
import com.stip.stip.iphome.model.IpListingItem
import com.stip.stip.R

class IpHomeInfoDetailFragment : Fragment() {

    private var _binding: FragmentIpHomeInfoDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var ipHomeInfoDetailAdapter: IpHomeInfoDetailAdapter
    private var currentItem: IpListingItem? = null

    companion object {
        private const val ARG_ITEM = "ip_listing_item"

        fun newInstance(item: IpListingItem): IpHomeInfoDetailFragment {
            return IpHomeInfoDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_ITEM, IpListingItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_ITEM) as? IpListingItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpHomeInfoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentItem?.let { item ->
            // 법인명, 대표자
            binding.tvValueCorporationName?.text = item.companyName
            binding.tvValueRepresentative?.text = item.representative ?: "-"

            // 어댑터 설정
            ipHomeInfoDetailAdapter = IpHomeInfoDetailAdapter(listOf(item))
            binding.recyclerViewDetailInfo.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ipHomeInfoDetailAdapter
            }

            // 링크 클릭 리스너 설정
            binding.linkDigitalIp.setOnClickListener {
                openExternalLink(item.digitalIpLink, getString(R.string.no_ip_link))
            }

            binding.linkHomepage.setOnClickListener {
                openExternalLink(item.homepageLink, getString(R.string.no_homepage_link))
            }

            binding.linkBusinessPlan.setOnClickListener {
                openExternalLink(item.businessPlanLink, getString(R.string.no_business_plan_link))
            }

            binding.linkRelatedVideo.setOnClickListener {
                openExternalLink(item.relatedVideoLink, getString(R.string.no_related_video_link))
            }

        } ?: run {
            Toast.makeText(context, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
        }
    }


    private fun openExternalLink(url: String?, errorMessage: String) {
        if (!url.isNullOrBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()))
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "링크를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "잘못된 링크입니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.recyclerViewDetailInfo?.adapter = null
        _binding = null
    }
}
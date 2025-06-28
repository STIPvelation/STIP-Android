package com.stip.ipasset.usd.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentIpAssetUsdDepositBinding
import com.stip.stip.databinding.LayoutDepositUsdBinding
import com.stip.ipasset.usd.model.DepositViewModel

import com.stip.ipasset.extension.dpToPx
import com.stip.ipasset.fragment.BaseFragment
import com.stip.ipasset.model.IpAsset
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * USD 입금을 처리하는 Fragment
 * 계좌 정보 목록을 표시합니다.
 */
@AndroidEntryPoint
class USDDepositFragment : BaseFragment<FragmentIpAssetUsdDepositBinding>() {
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentIpAssetUsdDepositBinding {
        return FragmentIpAssetUsdDepositBinding.inflate(inflater, container, false)
    }
    private val args by navArgs<com.stip.ipasset.usd.fragment.USDDepositFragmentArgs>()
    private val ipAsset: IpAsset get() = args.ipAsset
    // Default to USD if currencyCode property doesn't exist
    private val currencyCode: String get() = "USD"

    private val viewModel by viewModels<DepositViewModel>()

    private fun bind() {
        val binding = binding ?: return
        
        // 뒤로 가기 버튼 설정
        binding.backButton.setOnClickListener {
            // 액티비티의 헤더는 계속 숨긴 상태로 유지 (USDTransactionFragment로 돌아갈 것이므로)
            
            // 정상적인 backstack pop 처리
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 화면 제목 설정
        binding.screenTitle.text = "$currencyCode ${getString(R.string.common_deposit_action)}"
    }

    // 레이아웃 관련 UI 설정
    private fun setupBankAccounts() {
        val binding = binding ?: return
        
        // KB Bank account copy button
        binding.kbCopyButton.setOnClickListener {
            val kbAccountNumber = binding.kbAccountNumber.text.toString()
            copyToClipboard(kbAccountNumber)
            showCopySuccessMessage(binding.kbCopySuccessMessage)
        }
        
        // Shinhan Bank account copy button
        binding.shCopyButton.setOnClickListener {
            val shAccountNumber = binding.shAccountNumber.text.toString()
            copyToClipboard(shAccountNumber)
            showCopySuccessMessage(binding.shCopySuccessMessage)
        }
        
        // Woori Bank account copy button
        binding.wrCopyButton.setOnClickListener {
            val wrAccountNumber = binding.wrAccountNumber.text.toString()
            copyToClipboard(wrAccountNumber)
            showCopySuccessMessage(binding.wrCopySuccessMessage)
        }
    }
    
    // Helper method to copy text to clipboard
    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("account_number", text)
        clipboardManager.setPrimaryClip(clipData)
    }
    
    // Helper method to show and then hide the success message
    private fun showCopySuccessMessage(messageView: View) {
        messageView.visibility = View.VISIBLE
        messageView.postDelayed({
            messageView.visibility = View.GONE
        }, 2000) // Hide after 2 seconds
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 상위 액티비티의 헤더 숨기기 (직접 진입 시에도 확실히 헤더가 보이지 않도록)
        val headerLayout = requireActivity().findViewById<View>(R.id.headerLayout)
        headerLayout?.visibility = View.GONE
        
        bind()
        setupBankAccounts()

        // USD 계좌 정보 가져오기
        viewModel.fetchAccountInfo(currencyCode)
    }

    // Removed duplicate inflate method
}

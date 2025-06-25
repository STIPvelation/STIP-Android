package com.stip.stip.more.fragment.ipentertainment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreIpDonationBinding
import com.stip.stip.more.activity.IPDonationPlanActivity
import com.stip.stip.more.activity.MoreIpDonationAgreementActivity

class IPDonationFragment : Fragment() {

    private var _binding: FragmentMoreIpDonationBinding? = null
    private val binding get() = _binding!!
    
    // ActivityResultLauncher를 사용하여 파일 선택 결과 처리
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreIpDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerFilePickerLauncher()
        setupFileUploadSection()
        setupDocumentLinks()
        setupProceedButton()
    }
    
    private fun registerFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // 선택된 파일 처리 로직
                    val fileName = uri.lastPathSegment ?: "선택한 파일"
                    Toast.makeText(context, "파일이 선택되었습니다: $fileName", Toast.LENGTH_SHORT).show()
                    // TODO: 파일 이름을 표시할 TextView 추가 필요
                }
            }
        }
    }
    

    
    private fun setupFileUploadSection() {
        binding.fileUploadSection.setOnClickListener {
            // 파일 선택기 열기
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf", 
                "image/jpeg", 
                "image/png"
            ))
            try {
                filePickerLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "파일 탐색기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupDocumentLinks() {
        binding.ipPlanCard.setOnClickListener {
            // IP 기부 활용 계획 화면으로 이동
            val intent = Intent(requireContext(), IPDonationPlanActivity::class.java)
            startActivity(intent)
        }
        
        binding.ipAgreementCard.setOnClickListener {
            // IP 기부 동의서 정보 표시
            Toast.makeText(context, "IP 기부 동의서 정보입니다.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupProceedButton() {
        binding.btnProceedToTerms.setOnClickListener {
            // IP 기부 동의서 화면으로 이동
            val intent = Intent(requireContext(), MoreIpDonationAgreementActivity::class.java)
            startActivity(intent)
        }
    }
    


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // onActivityResult는 ActivityResultLauncher로 대체되어 더 이상 필요하지 않습니다.
}

package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.stip.stip.databinding.FragmentIpSwapApplicationBinding
import com.stip.stip.more.fragment.ipentertainment.model.SwapModel

class IPSwapApplicationFragment : Fragment() {
    private var _binding: FragmentIpSwapApplicationBinding? = null
    private val binding get() = _binding!!
    
    // 네비게이션 args를 통해 SwapModel 데이터 받기
    private val args: IPSwapApplicationFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpSwapApplicationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 이전 화면에서 전달받은 SwapModel 데이터 가져오기
        val swapItem = args.swapItem
        
        setupViews(swapItem)
        setupListeners()
    }
    
    private fun setupViews(swapItem: SwapModel) {
        // IP 번호 필드에 이전 화면에서 받은 정보 미리 채우기
        binding.etIpNumberInput.setText("US-2357-4566")
    }
    
    private fun setupListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // 업로드 버튼
        binding.btnUpload.setOnClickListener {
            if (isFormValid()) {
                // 실제로는 여기서 API 호출을 통해 서버에 스왑 신청 정보를 전송할 것
                Toast.makeText(requireContext(), "스왑 신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun isFormValid(): Boolean {
        // 필수 입력 필드 유효성 검사
        return !binding.etIpCertificate.text.isNullOrEmpty() &&
                !binding.etContact.text.isNullOrEmpty() &&
                !binding.etIpNumberInput.text.isNullOrEmpty() &&
                !binding.etSwapReason.text.isNullOrEmpty() &&
                binding.checkboxAgree.isChecked
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.stip.stip.more.fragment.ipentertainment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.stip.stip.databinding.FragmentIpSwapDetailBinding
import com.stip.stip.more.fragment.ipentertainment.model.SwapModel

class IPSwapDetailFragment : Fragment() {
    private var _binding: FragmentIpSwapDetailBinding? = null
    private val binding get() = _binding!!
    
    // 네비게이션 args를 통해 SwapModel 데이터 받기
    private val args: IPSwapDetailFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIpSwapDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 이전 화면에서 전달받은 SwapModel 데이터 가져오기
        val swapItem = args.swapItem
        
        setupViews(swapItem)
        setupListeners(swapItem)
    }
    
    private fun setupViews(swapItem: SwapModel) {
        with(binding) {
            // IP 번호와 타이틀 설정
            textIpNumber.text = "US-2357-4566" // 고정 IP 번호를 사용하여 iOS 화면과 동일하게 표시
            textIpTitle.text = "스타벅스 아프리카 대륙 독점권"
            
            // 기본 정보 필드 설정
            textIpNumberValue.text = "US-2357-4566"
            textIpRegistrationOffice.text = "미국 상표청"
            textRemainingPeriod.text = "20년"
            textContactPerson.text = "버라사 흔아 이름"
            textContactEmail.text = "legal@starbucks.com"
            
            // 스왑 정보 설정
            textIpDescription.text = "아프리카 대륙 전체에 대한 스타벅스 브랜드 독점권입니다."
        }
    }
    
    private fun setupListeners(swapItem: SwapModel) {
        // 스왑 신청 버튼
        binding.btnSwapRequest.setOnClickListener {
            // 스왑 신청 화면으로 이동 - 프래그먼트 트랜잭션 사용
            val applicationFragment = IPSwapApplicationFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("swapItem", swapItem)
                }
            }
            
            // 현재 프래그먼트가 표시되는 컨테이너의 ID 가져오기
            val containerId = (view?.parent as? ViewGroup)?.id ?: android.R.id.content
            
            // 프래그먼트 트랜잭션으로 교체
            parentFragmentManager.beginTransaction()
                .replace(containerId, applicationFragment)
                .addToBackStack(null)
                .commit()
        }
        
        // 관심 IP로 등록 버튼
        binding.btnAddToInterest.setOnClickListener {
            Toast.makeText(requireContext(), "관심 IP로 등록되었습니다.", Toast.LENGTH_SHORT).show()
            // 실제로는 관심 IP 등록 API 호출
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.stip.stip.ipinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stip.stip.databinding.DiplistinginfoBinding

class DipListingInfoFragment : Fragment() {

    private var _binding: DiplistinginfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DiplistinginfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 가기 버튼 클릭 시
        binding.buttonBackDip.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // 스크롤을 제일 위로 이동시키는 함수
    fun scrollToTop() {
        binding.scrollView.smoothScrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

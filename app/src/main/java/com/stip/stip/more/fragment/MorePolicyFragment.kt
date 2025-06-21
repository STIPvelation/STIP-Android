package com.stip.stip.more.fragment

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
        
        // 정책 타이틀 배열 가져오기
        val policyTitles = resources.getStringArray(R.array.policy_terms_title_array)
        
        // 레이아웃에서 모든 CardView를 찾아서 클릭 리스너 설정
        val cardViews = findAllCardViewsInContainer(binding.root)
        
        // 최대 16개 정책 카드에 대해 클릭 리스너 설정
        cardViews.take(16).forEach { cardView ->
            // 각 카드뷰의 텍스트 제목 찾기
            val cardTitle = findCardTitle(cardView)
            
            // 정책 타이틀 매핑 (카드뷰의 제목과 일치하는 정책 인덱스 찾기)
            val policyIndex = findPolicyIndex(cardTitle, policyTitles)
            
            cardView.setOnClickListener {
                showPolicyDialog(policyIndex)
            }
        }
    }
    
    // 카드뷰 내의 텍스트 제목 찾기
    private fun findCardTitle(cardView: CardView): String {
        // 카드뷰 내부에서 TextView 검색
        val textViews = mutableListOf<TextView>()
        findAllTextViews(cardView, textViews)
        
        // 텍스트뷰에서 정책 제목 찾기 - 첫 번째 텍스트뷰 사용
        return textViews.firstOrNull()?.text?.toString() ?: ""
    }
    
    // 타이틀에 해당하는 정책 인덱스 찾기
    private fun findPolicyIndex(cardTitle: String, policyTitles: Array<String>): Int {
        // 특수한 카드 타이틀에 대한 명시적 매핑 처리
        when {
            // 수수료 투명성 정책 매핑
            cardTitle.contains("수수료", ignoreCase = true) && cardTitle.contains("투명성", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("STIP 수수료 투명성 정책", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
            
            // 통신사 이용약관 매핑
            cardTitle.contains("통신사", ignoreCase = true) && cardTitle.contains("이용약관", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("인증사 서비스 이용약관", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
            
            // 이용약관 및 이용안내 매핑
            cardTitle.contains("이용약관", ignoreCase = true) && cardTitle.contains("이용안내", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("이용약관 및 이용안내", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
            
            cardTitle.contains("거래통화", ignoreCase = true) || cardTitle.contains("환전", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("거래 통화 및 환전방식", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
                
            // 개인정보 수집 동의 특별 처리
            cardTitle.equals("개인정보 수집 및 이용동의", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("개인정보 수집 및 이용동의", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
                
            // 인증시 개인정보 수집동의 특별 처리
            cardTitle.contains("인증시 개인정보 수집", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("인증시 개인정보 수집", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
                
            // 단순 개인정보 수집 동의
            cardTitle.contains("개인정보 수집", ignoreCase = true) && !cardTitle.contains("이용", ignoreCase = true) && !cardTitle.contains("인증", ignoreCase = true) -> {
                val idx = policyTitles.indexOfFirst { it.contains("개인정보 수집 동의", ignoreCase = true) && !it.contains("및 이용", ignoreCase = true) }
                return if (idx >= 0) idx else 0
            }
        }
        
        // 정책 타이틀에서 "(필수)" 같은 접두사를 제거한 핵심 키워드를 추출
        val cleanPolicyTitles = policyTitles.map { title ->
            title.replace(Regex("\\(필수\\)\\s*"), "").trim()
        }
        
        // 정규화된 카드 타이틀
        val normalizedCardTitle = cardTitle.trim()
        
        // 핵심 키워드 매칭
        val keywordMatches = mutableListOf<Pair<Int, Int>>() // 인덱스, 일치도 점수
        
        cleanPolicyTitles.forEachIndexed { index, cleanTitle ->
            val matchScore = calculateMatchScore(normalizedCardTitle, cleanTitle)
            if (matchScore > 0) {
                keywordMatches.add(Pair(index, matchScore))
            }
        }
        
        // 가장 높은 점수의 매치 사용
        return if (keywordMatches.isNotEmpty()) {
            keywordMatches.maxByOrNull { it.second }?.first ?: 0
        } else {
            0 // 매칭이 없으면 기본값 0 반환
        }
    }
    
    // 두 문자열 간의 키워드 일치도 계산
    private fun calculateMatchScore(str1: String, str2: String): Int {
        // 핵심 키워드 추출
        val keywords1 = str1.split(Regex("\\s+|및|\\("))
            .filter { it.length > 1 } // 짧은 단어 필터링
        val keywords2 = str2.split(Regex("\\s+|및|\\("))
            .filter { it.length > 1 }
        
        var score = 0
        
        // 키워드 교차 일치 점수
        for (keyword in keywords1) {
            if (keywords2.any { it.contains(keyword, ignoreCase = true) || keyword.contains(it, ignoreCase = true) }) {
                score += keyword.length // 키워드 길이에 비례한 점수 부여
            }
        }
        
        return score
    }
    
    // TextView 찾기 헬퍼 함수
    private fun findAllTextViews(view: View, textViews: MutableList<TextView>) {
        if (view is TextView) {
            textViews.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findAllTextViews(view.getChildAt(i), textViews)
            }
        }
    }
    
    private fun findAllCardViewsInContainer(container: ViewGroup): List<CardView> {
        val cards = mutableListOf<CardView>()
        
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            
            when (child) {
                is CardView -> cards.add(child)
                is ViewGroup -> cards.addAll(findAllCardViewsInContainer(child))
            }
        }
        
        return cards
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun showPolicyDialog(context: Context, index: Int) {
            // 두 개의 다른 정책 타이틀 배열 가져오기
            val policyTitles = context.resources.getStringArray(R.array.policy_terms_title_array)
            val signUpTitles = context.resources.getStringArray(R.array.sign_up_agree_title_array)
            val contents = context.resources.getStringArray(R.array.policy_terms_content_array)
            
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_policy_confirm, null)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()
            
            // 인덱스 범위 유효성 검사
            val safeIndex = if (index >= 0) index else 0
            
            // 기본적으로 policy_terms_title_array 사용
            var titleText = policyTitles.getOrNull(safeIndex) ?: "정책 제목 없음"
            val contentText = contents.getOrNull(safeIndex) ?: "본문 내용이 없습니다."
            
            // 정책 타이틀이 없으면 sign_up_agree_title_array에서 시도
            if (titleText == "정책 제목 없음" && safeIndex < signUpTitles.size) {
                titleText = signUpTitles[safeIndex]
            }
            val spannable = SpannableString(contentText)

            val koreanHeaderRegex = Regex("""제(1[0-9]|20|[1-9])조""")
            koreanHeaderRegex.findAll(contentText).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(AbsoluteSizeSpan(14, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val numberHeaderRegex = Regex("""(?m)^\s*((?:10|[1-9])\.)(.*?)""")
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
}

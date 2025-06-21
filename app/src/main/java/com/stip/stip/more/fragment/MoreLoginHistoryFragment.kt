package com.stip.stip.more.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stip.stip.R
import com.stip.stip.databinding.FragmentMoreLoginHistoryBinding
import com.stip.stip.more.adapter.LoginHistoryAdapter
import com.stip.stip.more.api.LoginHistoryItem
import com.stip.stip.signup.Constants
import com.stip.stip.signup.login.LoginActivity
import com.stip.stip.signup.utils.PreferenceUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoreLoginHistoryFragment : Fragment() {

    private lateinit var binding: FragmentMoreLoginHistoryBinding
    private val TAG = "LoginHistoryFragment"
    private val adapter = LoginHistoryAdapter(emptyList())
    
    // UI 컴포넌트 직접 참조
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoHistory: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreLoginHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // UI 컴포넌트 참조 가져오기
        recyclerView = binding.recyclerViewLoginHistory
        progressBar = view.findViewById(R.id.progressBar)
        textNoHistory = view.findViewById(R.id.textNoHistory)

        // RecyclerView 초기화
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 로그인 이력 데이터 로드
        loadLoginHistory()

        // ✅ 모든 기기에서 로그아웃 → 로그인 화면 이동
        binding.btnLogoutAll.setOnClickListener {
            logoutAllAndRedirect()
        }
    }

    private fun loadLoginHistory() {
        // 로딩 표시
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        textNoHistory.visibility = View.GONE

        // 인증 여부 확인
        val token = PreferenceUtil.getString(Constants.PREF_KEY_AUTH_TOKEN_VALUE)
        if (token.isBlank()) {
            // 로그인되지 않은 상태
            showNoHistoryMessage("로그인이 필요합니다")
            return
        }

        // API 호출 구현
        lifecycleScope.launch {
            try {
                // 실제 API 호출 코드 (http://34.64.170.83/swagger-ui/index.html 참조)
                // val api = RetrofitClient.loginHistoryService
                // val response = api.getLoginHistory()
                // response.onSuccess { data ->
                //     if (data.isEmpty()) {
                //         showNoHistoryMessage("로그인 이력이 없습니다")
                //     } else {
                //         updateLoginHistoryList(data)
                //     }
                // }.onError { error ->
                //     Log.e(TAG, "Error fetching login history", error)
                //     showNoHistoryMessage("로그인 이력을 불러오는 중 오류가 발생했습니다")
                // }
                
                // 임시 로딩 지연 (실제 구현 시 삭제)
                delay(1000)
                
                // 임시 처리: 데이터가 없는 상태 표시
                showNoHistoryMessage(getString(R.string.login_history_no_data))
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception when fetching login history", e)
                showNoHistoryMessage(getString(R.string.login_history_loading_error))
            }
        }
    }

    private fun updateLoginHistoryList(historyItems: List<LoginHistoryItem>) {
        progressBar.visibility = View.GONE
        
        if (historyItems.isEmpty()) {
            recyclerView.visibility = View.GONE
            textNoHistory.visibility = View.VISIBLE
            textNoHistory.text = getString(R.string.login_history_no_data)
        } else {
            recyclerView.visibility = View.VISIBLE
            textNoHistory.visibility = View.GONE
            adapter.updateItems(historyItems)
        }
    }

    private fun showNoHistoryMessage(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        textNoHistory.visibility = View.VISIBLE
        textNoHistory.text = message
    }

    private fun logoutAllAndRedirect() {
        progressBar.visibility = View.VISIBLE
        binding.btnLogoutAll.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // 실제 API 호출 코드 (모든 기기 로그아웃)
                // val api = RetrofitClient.authService
                // val response = api.logoutAllDevices()
                // response.onSuccess { 
                //     clearLocalSessionAndRedirect()
                // }.onError { error ->
                //     Log.e(TAG, "Error logging out from all devices", error)
                //     Toast.makeText(requireContext(), "로그아웃 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                //     progressBar.visibility = View.GONE
                //     binding.btnLogoutAll.isEnabled = true
                // }
                
                // 임시 로딩 지연 (실제 구현 시 삭제)
                delay(1000)
                
                // 임시 처리: 직접 로그아웃 처리
                clearLocalSessionAndRedirect()
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception when logging out from all devices", e)
                Toast.makeText(requireContext(), "로그아웃 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                binding.btnLogoutAll.isEnabled = true
            }
        }
    }
    
    private fun clearLocalSessionAndRedirect() {
        // 로컬 세션 정보 삭제
        PreferenceUtil.putString(Constants.PREF_KEY_AUTH_TOKEN_VALUE, "")
        // PreferenceUtil.KEY_MEMBER_INFO는 private이므로 직접 상수 문자열 사용
        PreferenceUtil.remove("member_info_json")
        
        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

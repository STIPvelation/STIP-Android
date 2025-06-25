package com.stip.stip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.stip.stip.signup.Constants
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // 주입받을 의존성 있으면 여기에 추가
) : ViewModel() {

    // --- 인증 여부 확인 ---
    val isAuthenticated: Boolean
        get() = PreferenceUtil.getString(key = Constants.PREF_KEY_AUTH_TOKEN_VALUE).isNotEmpty()

    // --- 헤더 제목 관리 ---
    private val _headerTitle = MutableLiveData<String>()
    val headerTitle: LiveData<String> = _headerTitle

    fun updateHeaderTitle(title: String) {
        if (_headerTitle.value != title) {
            _headerTitle.value = title
        }
    }

    // --- 헤더 아이콘 리소스 관리 ---
    private val _navigationIconRes = MutableLiveData<Int>()
    val navigationIconRes: LiveData<Int> = _navigationIconRes

    fun updateNavigationIcon(resId: Int) {
        _navigationIconRes.value = resId
    }

    // --- 헤더 아이콘 클릭 리스너 관리 ---
    private val _navigationClickListener = MutableLiveData<() -> Unit>()
    val navigationClickListener: LiveData<() -> Unit> = _navigationClickListener

    fun updateNavigationClickListener(listener: () -> Unit) {
        _navigationClickListener.value = listener
    }

    // ✅ 뒤로가기 기본 설정 한 번에 적용하는 유틸 함수
    fun enableBackNavigation(
        iconResId: Int = R.drawable.ic_arrow_return,
        onClick: (() -> Unit)? = null
    ) {
        updateNavigationIcon(iconResId)
        updateNavigationClickListener(onClick ?: { })
    }

    // ✅ 헤더 초기화용
    fun clearNavigation() {
        updateNavigationIcon(0)
        updateNavigationClickListener { }
    }
    // --- 회원정보 관리 ---
    private val _memberInfo = MutableLiveData<com.stip.stip.model.MemberInfo?>()
    val memberInfo: LiveData<com.stip.stip.model.MemberInfo?> = _memberInfo

    // TODO: saveMemberInfo
    fun setMemberInfo(info: com.stip.stip.model.MemberInfo) {
        _memberInfo.value = info
        com.stip.stip.signup.utils.PreferenceUtil.saveMemberInfo(info)
    }

    fun loadMemberInfo() {
        // 캐시된 회원정보 확인
        val info = com.stip.stip.signup.utils.PreferenceUtil.getMemberInfo()

        if (info != null) {
            _memberInfo.value = info
            Log.d("MainViewModel", "캐시된 회원정보 불러옴")
        } else if (isAuthenticated) {
            // 인증된 상태지만 캐시된 회원정보가 없는 경우, API에서 정보 가져오기
            Log.d("MainViewModel", "API에서 회원정보 조회 시도")
            
            // 실제 API 연동 구현
            viewModelScope.launch {
                try {
                    // 실제 구현 시 API 서비스 사용
                    // 예시: http://34.64.170.83/swagger-ui/index.html 참조
                    // val token = PreferenceUtil.getString(Constants.PREF_KEY_AUTH_TOKEN_VALUE)
                    // val memberService = RetrofitClient.getMemberService(token)
                    // val response = memberService.getMemberInfo()
                    // 실제 API 구현 전 임시 지연 시뮬레이션
                    
                    // MemberRepository를 통해 회원 정보 API 조회
                    val memberRepository = com.stip.stip.api.repository.MemberRepository()
                    val apiMemberInfo = memberRepository.getMemberInfo()
                    _memberInfo.value = apiMemberInfo

                    /* 기존 더미 데이터 - 주석 처리
                    // 9자리 회원번호 생성 또는 가져오기
                    val savedMemberId = com.example.stipandroid.signup.utils.PreferenceUtil.getString("PREF_KEY_MEMBER_ID")
                    val memberId = if (savedMemberId.isBlank()) {
                        // 새로운 9자리 랜덤 ID 생성
                        val random = java.security.SecureRandom()
                        val newMemberId = (100_000_000 + random.nextInt(900_000_000)).toString()
                        // 생성된 ID 저장
                        com.example.stipandroid.signup.utils.PreferenceUtil.putString("PREF_KEY_MEMBER_ID", newMemberId)
                        Log.d("MainViewModel", "새로운 회원 ID 생성: $newMemberId")
                        newMemberId
                    } else {
                        // 이미 저장된 ID 사용
                        Log.d("MainViewModel", "기존 회원 ID 사용: $savedMemberId")
                        savedMemberId
                    }
                    
                    // API에서 가져온 응답 사용 (실제 API 구현 시 실제 데이터로 대체)
                    val memberInfo = com.example.stipandroid.model.MemberInfo(
                        name = "API 연동 사용자",  // 실제로는 API 응답에서 가져온 이름 사용
                        email = "api@stipvelation.com",  // API 응답에서 가져온 이메일 사용
                        phone = "010-9876-5432",  // API 응답에서 가져온 전화번호 사용
                        memberId = memberId,  // 생성된 또는 저장된 회원 ID 사용
                        passportName = "API USER",  // API 응답에서 가져온 영문이름 사용
                        birthDate = "1990-01-01",  // API 응답에서 가져온 생년월일 사용
                        bankInfo = "신한은행 110-123-456789",  // API 응답에서 가져온 은행정보 사용
                        address = "서울시 강남구 테헤란로 123",  // API 응답에서 가져온 주소 사용
                        job = "개발자",  // API 응답에서 가져온 직업 사용
                        jobTitle = "STIP",  // API 응답에서 가져온 회사명 사용
                        jobAddress = "서울시 강남구 테헤란로 321"  // API 응답에서 가져온 직장주소 사용
                    )
                    
                    _memberInfo.value = memberInfo
                    // TODO: saveMemberInfo
                    com.example.stipandroid.signup.utils.PreferenceUtil.saveMemberInfo(memberInfo)
                    */
                    
                } catch (e: Exception) {
                    Log.e("MainViewModel", "API에서 회원정보 가져오기 오류", e)
                    _memberInfo.value = null
                }
            }
        } else {
            // 로그인되지 않은 경우 null 처리
            _memberInfo.value = null
            Log.d("MainViewModel", "로그인되지 않은 상태 - 회원정보 null 처리")
        }
    }
}

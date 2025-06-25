package com.stip.stip.signup.signup.pin.finish

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import com.stip.stip.signup.Constants
import com.stip.stip.signup.api.repository.auth.AuthRepository
import com.stip.stip.signup.api.repository.member.MemberRepository
import com.stip.stip.signup.base.BaseViewModel
import com.stip.stip.signup.model.RequestAuthLogin
import com.stip.stip.signup.model.ResponseAuthLogin
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpPinNumberFinishViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository
): BaseViewModel() {

    private val _errorLiveData = MutableLiveData<Int>()
    val errorLiveData: LiveData<Int> get() = _errorLiveData

    private val _authLoginLiveData = MutableLiveData<ResponseAuthLogin>()
    val authLoginLiveData: LiveData<ResponseAuthLogin> get() = _authLoginLiveData
    /** 로그인 처리 */
    fun requestPostAuthLogin(requestAuthLogin: RequestAuthLogin) {
        showProgress()

        viewModelScope.launch {
            val response = authRepository.postAuthLogin(requestAuthLogin)
            response.suspendOnSuccess {
                this.response.body()?.let {
                    PreferenceUtil.saveToken(it.accessToken)
                    _authLoginLiveData.value = it
                }
            }.suspendOnError {
                Log.e("Error",this.errorBody?.string() ?: "error")
                val errorMessage = when (this.response.code()) {
                    409 -> Constants.NETWORK_DUPLICATE_ERROR_CODE
                    else -> Constants.NETWORK_SERVER_ERROR_CODE
                }
                _errorLiveData.value = errorMessage

            }.suspendOnException {
                Log.e("Exception",this.message.toString())
                _errorLiveData.value = Constants.NETWORK_ERROR_CODE
            }

            hideProgress()
        }
    }


    private val _memberDeleteLiveData = MutableLiveData<Boolean>()
    val memberDeleteLiveData: LiveData<Boolean> get() = _memberDeleteLiveData
    /** 회원 탈퇴 */
    fun requestDeleteMembers(di: String) {
        showProgress()

        viewModelScope.launch {
            val response = memberRepository.deleteMembers(di)
            response.suspendOnSuccess {
                if (this.response.code() == 204) {
                    _memberDeleteLiveData.value = true
                } else {
                    // 예상치 못한 응답 코드 처리 (예: 200 등)
                    _errorLiveData.value = Constants.NETWORK_SERVER_ERROR_CODE
                }
            }.suspendOnError {
                Log.e("Error",this.errorBody?.string() ?: "error")
                val errorMessage = when (this.response.code()) {
                    409 -> Constants.NETWORK_DUPLICATE_ERROR_CODE
                    else -> Constants.NETWORK_SERVER_ERROR_CODE
                }
                _errorLiveData.value = errorMessage

            }.suspendOnException {
                Log.e("Exception",this.message.toString())
                _errorLiveData.value = Constants.NETWORK_ERROR_CODE
            }

            hideProgress()
        }
    }

}
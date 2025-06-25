package com.stip.stip.signup.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import com.stip.stip.signup.Constants
import com.stip.stip.signup.api.repository.auth.AuthRepository
import com.stip.stip.signup.base.BaseViewModel
import com.stip.stip.signup.model.RequestAuthLogin
import com.stip.stip.signup.model.ResponseAuthLogin
import com.stip.stip.signup.utils.PreferenceUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {

    private val _pinNumberBasicSettingLiveData = MutableLiveData<Boolean>()
    val pinNumberBasicSettingLiveData: LiveData<Boolean> get() = _pinNumberBasicSettingLiveData
    /** PIN 번호로 기본 로그인 설정 */
    fun setPinNumberBasicSetting(isCheck: Boolean) {
        _pinNumberBasicSettingLiveData.value = isCheck
    }

    private val _biometricAuthBasicSettingLiveData = MutableLiveData<Boolean>()
    val biometricAuthBasicSettingLiveData: LiveData<Boolean> get() = _biometricAuthBasicSettingLiveData
    /** 생체인증으로 기본 로그인 설정 */
    fun setBiometricAuthBasicSetting(isCheck: Boolean) {
        _biometricAuthBasicSettingLiveData.value = isCheck
    }



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
                    else -> Constants.NETWORK_LOGIN_FAIL_CODE
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
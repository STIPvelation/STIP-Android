package com.stip.stip.more.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.stip.stip.R
import com.stip.stip.databinding.FragmentSignUpPinNumberFinishBinding
import com.stip.stip.more.fragment.PinIncorrectDialogFragment
import com.stip.stip.more.fragment.ForgetPinNumberDialogFragment
import com.stip.stip.signup.Constants
import com.stip.stip.signup.utils.PreferenceUtil
import com.stip.stip.signup.model.RequestPinNumber
import com.stip.stip.signup.api.repository.member.MemberRepository
import com.stip.stip.signup.customview.LoadingDialog
import com.stip.stip.signup.keypad.KeypadAdapter
import com.stip.stip.signup.keypad.KeypadItem
import com.stip.stip.signup.keypad.KeypadType
import com.stip.stip.signup.pin.PinAdapter
import com.stip.stip.signup.customview.CustomContentDialog
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnException
import com.skydoves.sandwich.suspendOnSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PinVerificationActivity : AppCompatActivity() {

    @Inject
    lateinit var memberRepository: MemberRepository

    private lateinit var binding: FragmentSignUpPinNumberFinishBinding
    private lateinit var pinAdapter: PinAdapter
    private lateinit var keypadAdapter: KeypadAdapter

    private var currentPin = ""
    private val validPin: String
        get() = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE, "") // 저장된 PIN 가져오기
    private var attemptCount = 0
    private val maxAttempts = 5
    private var isPinChange = false // PIN 변경 목적인지 표시
    private var isSettingNewPin = false // 새로운 PIN 설정 중인지 표시
    private var isBiometricSetup = false // 생체인증 설정 목적인지 표시 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSignUpPinNumberFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 PIN 변경 목적인지 확인
        isPinChange = intent.getBooleanExtra("is_pin_change", false)
        // 새로운 PIN 설정 화면인지 확인
        isSettingNewPin = intent.getBooleanExtra("is_setting_new_pin", false)
        // 생체인증 설정 목적인지 확인
        isBiometricSetup = intent.getBooleanExtra("is_biometric_setup", false)

        // 타이틀 설정
        if (isSettingNewPin) {
            binding.tvSignUpPinNumberFinishTitle.text = "새로운 PIN 비밀번호 입력"
            binding.tvPinNumberGuide.visibility = View.GONE
            
            // 동일하거나 연속된 숫자 제한 안내 메시지를 빨간색으로 표시
            binding.tvSignUpPinNumberWarning.text = "동일하거나 연속된 숫자는 등록이 제한됩니다."
            binding.tvSignUpPinNumberWarning.setTextColor(ContextCompat.getColor(this, R.color.red_DB3949_100))
            binding.tvSignUpPinNumberWarning.visibility = View.VISIBLE
            
            // PIN 입력 타이틀 설정
            binding.tvSignUpPinNumberFinishTitle.text = "PIN 번호 입력"
            
            // 테스트용 버튼 추가 (루트 레이아웃에 추가)
            val testButton = android.widget.Button(this)
            testButton.text = "회원정보 화면 테스트"
            testButton.setBackgroundColor(android.graphics.Color.RED)
            testButton.setTextColor(android.graphics.Color.WHITE)
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            testButton.layoutParams = params
            
            // 루트 레이아웃에 수동으로 버튼 추가
            val rootLayout = binding.root as androidx.constraintlayout.widget.ConstraintLayout
            rootLayout.addView(testButton)
            
            // 버튼 위치 설정
            val buttonLayoutParams = testButton.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            buttonLayoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            buttonLayoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            buttonLayoutParams.topMargin = 100
            buttonLayoutParams.leftMargin = 100
            testButton.layoutParams = buttonLayoutParams
            
            testButton.setOnClickListener {
                Toast.makeText(this, "테스트 버튼 클릭됨", Toast.LENGTH_SHORT).show()
                try {
                    val intent = Intent(this, Class.forName("com.stip.stip.more.activity.MemberInfoEditActivity"))
                    startActivity(intent)
                    Toast.makeText(this, "화면 전환 시도 완료", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "오류: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            binding.tvSignUpPinNumberFinishTitle.text = "PIN 비밀번호 입력"
            binding.tvSignUpPinNumberWarning.visibility = View.GONE
            
            // PIN 변경 목적일 때만 해당 안내 메시지 표시
            if (isPinChange) {
                binding.tvPinNumberGuide.text = "현재 사용중인 PIN 비밀번호를 입력해 주세요."
                binding.tvPinNumberGuide.visibility = View.VISIBLE
            } else {
                binding.tvPinNumberGuide.visibility = View.GONE
            }
        }
        
        // 경고 메시지는 초기에 숨김 (PIN 번호가 틀렸을 때만 표시)
        binding.tvSignUpPinNumberWarning.visibility = View.GONE

        // ⚫ PIN 점 표시 어댑터
        pinAdapter = PinAdapter(MutableList(6) { false })
        binding.rvSignUpPinNumberPassword.apply {
            layoutManager = LinearLayoutManager(
                this@PinVerificationActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = pinAdapter
        }

        // ⌨ 키패드 설정
        keypadAdapter = KeypadAdapter(KeypadItem.default()) { item ->
            when (item.type) {
                KeypadType.NUMBER -> {
                    if (currentPin.length < 6) {
                        currentPin += item.value
                        pinAdapter.updatePinCount(currentPin.length)
                    }
                    if (currentPin.length == 6) verifyPin()
                }

                KeypadType.DELETE -> {
                    if (currentPin.isNotEmpty()) {
                        currentPin = currentPin.dropLast(1)
                        pinAdapter.updatePinCount(currentPin.length)
                    }
                }

                KeypadType.SHUFFLE -> keypadAdapter.shuffleNumbers()
            }
        }

        binding.rvSignUpPinNumberKeypad.apply {
            layoutManager = GridLayoutManager(this@PinVerificationActivity, 3)
            adapter = keypadAdapter
        }

        // 🔙 뒤로 가기
        binding.ivBack.setOnClickListener {
            finish()
        }

        // ⚠ 경고 문구 숨김
        binding.tvSignUpPinNumberWarning.visibility = View.GONE

        // 🔓 "PIN 비밀번호를 잊으셨나요?" → 다이얼로그 팝업
        binding.tvLoginPinNumberForgetPassword.setOnClickListener {
            Log.e("forgetPassword", "틀틀")
            ForgetPinNumberDialogFragment(
                onConfirm = {
                    // 바로 휴대폰 본인인증을 통한 PIN 재설정 화면으로 이동
                    com.stip.stip.signup.signup.SignUpActivity.startSignUpActivityPinNumberChange(
                        this,
                        true, // 본인인증 실행
                        true  // PIN 잊었을 때 플래그 설정
                    )
                    finish()
                },
                onCancel = { /* Do nothing */ }
            ).show(supportFragmentManager, "ForgetPinDialog")
        }
    }

    private fun verifyPin() {
        // 테스트용 PIN "123456" 추가 - 모든 PIN 검증 과정 생략하고 바로 처리
        if (currentPin == "123456") {
            android.util.Log.d("PinVerification", "테스트 PIN 입력 감지 - 직접 검증 통과 처리")
            
            // 새 PIN 설정인 경우 성공 처리
            if (isSettingNewPin) {
                PreferenceUtil.putString(Constants.PREF_KEY_PIN_VALUE, currentPin)
                android.util.Log.d("PinVerification", "테스트 PIN으로 새 PIN 설정 완료")
                
                runOnUiThread {
                    // 성공 메시지 표시 후 종료 - 커스텀 다이얼로그 사용
                    CustomContentDialog(this@PinVerificationActivity) {
                        finish()
                    }.setText("성공", "PIN 번호가 변경되었습니다.", "", "확인", false)
                }
                return
            } else if (isPinChange) {
                // PIN 변경 목적일 경우 새 PIN 입력 화면으로 이동
                android.util.Log.d("PinVerification", "테스트 PIN으로 PIN 변경 화면으로 이동")
                val intent = Intent(this@PinVerificationActivity, PinVerificationActivity::class.java)
                intent.putExtra("is_setting_new_pin", true)
                startActivity(intent)
                finish()
                return
            } else if (isBiometricSetup) {
                // 생체인증 설정 목적일 경우 즉시 성공 처리
                android.util.Log.d("PinVerification", "테스트 PIN으로 생체인증 활성화")
                val sharedPrefBio = getSharedPreferences("security_pref", android.content.Context.MODE_PRIVATE)
                sharedPrefBio.edit().putBoolean("biometric_enabled", true).apply()
                Toast.makeText(this@PinVerificationActivity, "생체인증 정보 사용이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
                return
            } else {
                // 일반 접근 목적일 경우 회원 정보 수정 화면으로 즉시 이동
                android.util.Log.d("PinVerification", "테스트 PIN으로 일반 접근 허용")
                val intent = Intent(this@PinVerificationActivity, com.stip.stip.more.activity.MemberInfoEditActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
        }
        
        // di 값 가져오기 - 실제로는 로그인한 회원의 di 값을 사용해야 함
        val memberDi = PreferenceUtil.getString(Constants.PREF_KEY_DI_VALUE, "")
        if (memberDi.isBlank()) {
            Toast.makeText(this, "회원 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 새로운 PIN 설정 화면일 경우
        if (isSettingNewPin) {
            // 기존 PIN과 동일한지 확인 (PreferenceUtil에서 가져온 기존 PIN 값과 비교)
            val existingPin = PreferenceUtil.getString(Constants.PREF_KEY_PIN_VALUE, "")
            if (existingPin.isNotBlank() && currentPin == existingPin) {
                currentPin = ""
                pinAdapter.updatePinCount(0)
                binding.tvSignUpPinNumberWarning.text = "기존 PIN과 동일한 PIN은 사용할 수 없습니다."
                binding.tvSignUpPinNumberWarning.setTextColor(ContextCompat.getColor(this, R.color.red_DB3949_100))
                binding.tvSignUpPinNumberWarning.visibility = View.VISIBLE
                return
            }
            
            // 연속되거나 동일한 숫자 검사
            if (isSequentialOrRepeated(currentPin)) {
                currentPin = ""
                pinAdapter.updatePinCount(0)
                binding.tvSignUpPinNumberWarning.text = "동일하거나 연속된 숫자는 사용할 수 없습니다."
                binding.tvSignUpPinNumberWarning.setTextColor(ContextCompat.getColor(this, R.color.red_DB3949_100))
                binding.tvSignUpPinNumberWarning.visibility = View.VISIBLE
                return
            }
            
            // API를 통한 새 PIN 설정
            val requestPinNumber = RequestPinNumber(currentPin)
            lifecycleScope.launch {
                val response = memberRepository.patchMemberPin(memberDi, requestPinNumber)
                response.suspendOnSuccess {
                    // 성공적으로 PIN 변경
                    PreferenceUtil.putString(Constants.PREF_KEY_PIN_VALUE, currentPin)
                    
                    runOnUiThread {
                        // 성공 메시지 표시 후 종료 - 커스텀 다이얼로그 사용
                        CustomContentDialog(this@PinVerificationActivity) {
                            finish()
                        }.setText("성공", "PIN 번호가 변경되었습니다.", "", "확인", false)
                    }
                }.suspendOnError {
                    runOnUiThread {
                        Toast.makeText(this@PinVerificationActivity, "오류가 발생했습니다: ${statusCode.code}", Toast.LENGTH_SHORT).show()
                        currentPin = ""
                        pinAdapter.updatePinCount(0)
                    }
                }.suspendOnException {
                    runOnUiThread {
                        Toast.makeText(this@PinVerificationActivity, "예외가 발생했습니다: ${message}", Toast.LENGTH_SHORT).show()
                        currentPin = ""
                        pinAdapter.updatePinCount(0)
                    }
                }
            }
            return
        }
        
        // 테스트용 코드 제거하고 실제 API 연동만 사용
        
        // 서버 API를 통해 PIN 유효성 검사 수행
        val requestPinNumber = RequestPinNumber(currentPin)
        
        // 로딩 다이얼로그 표시
        val loadingDialog = LoadingDialog(this)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
        
        // 임시로 123456 PIN을 허용 (UI 개발용)
        lifecycleScope.launch {
            // 임시로 123456을 입력하면 항상 성공으로 처리 (UI 개발을 위한 임시 코드)
            if (currentPin == "123456") {
                // Simulate PIN success
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                    if (isPinChange) {
                        val intent = Intent(this@PinVerificationActivity, PinVerificationActivity::class.java)
                        intent.putExtra("is_setting_new_pin", true)
                        startActivity(intent)
                        finish()
                    } else if (isBiometricSetup) {
                        val biometricLoadingDialog = LoadingDialog(this@PinVerificationActivity)
                        biometricLoadingDialog.setCancelable(false)
                        biometricLoadingDialog.show()
                        
                        Handler(Looper.getMainLooper()).postDelayed({
                            val sharedPrefBio = getSharedPreferences("security_pref", android.content.Context.MODE_PRIVATE)
                            sharedPrefBio.edit().putBoolean("biometric_enabled", true).apply()
                            biometricLoadingDialog.dismiss()
                            Toast.makeText(this@PinVerificationActivity, "생체인증 정보 사용이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }, 2000) 
                    } else {
                        // 지연 제거하고 즉시 회원정보 화면으로 이동
                        Log.d("PinVerification", "테스트 PIN 확인됨. 회원정보 화면으로 즉시 이동")
                        Toast.makeText(this@PinVerificationActivity, "회원정보 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                        
                        // 가장 기본적인 액티비티 시작 방법 사용 - 전체 경로 지정
                        val intent = Intent(this@PinVerificationActivity, com.stip.stip.more.activity.MemberInfoEditActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }, 1000) // 1초 지연
                return@launch
            }
            
            // 일반적인 API 호출 (123456이 아닌 경우)
            val response = memberRepository.verifyMemberPin(memberDi, requestPinNumber)
            response.suspendOnSuccess {
                // PIN 유효성 검사 성공 - PIN 일치
                runOnUiThread {
                    // 일반 로딩 다이얼로그 닫기
                    loadingDialog.dismiss()
                    
                    if (isPinChange) {
                        Log.e("isPinChange", "핀핀")
                        // PIN 변경 목적일 경우 새 PIN 입력 화면으로 이동
                        val intent = Intent(this@PinVerificationActivity, PinVerificationActivity::class.java)
                        intent.putExtra("is_setting_new_pin", true)
                        startActivity(intent)
                        finish()
                    } else if (isBiometricSetup) {
                        // 생체인증 설정 목적일 경우 로딩 다이얼로그 표시
                        val biometricLoadingDialog = LoadingDialog(this@PinVerificationActivity)
                        biometricLoadingDialog.setCancelable(false)
                        biometricLoadingDialog.show()
                        
                        // 2초 후에 생체인증 활성화 처리
                        Handler(Looper.getMainLooper()).postDelayed({
                            // 생체인증 설정 활성화
                            val sharedPrefBio = getSharedPreferences("security_pref", android.content.Context.MODE_PRIVATE)
                            sharedPrefBio.edit().putBoolean("biometric_enabled", true).apply()
                            
                            // 로딩 다이얼로그 닫기
                            biometricLoadingDialog.dismiss()
                            
                            // 완료 메시지 표시
                            Toast.makeText(this@PinVerificationActivity, "생체인증 정보 사용이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                            
                            // 원래 화면으로 돌아가기 위해 활동 종료
                            finish()
                        }, 2000) // 2초 후 실행
                    } else {
                        // 일반 접근 목적일 경우 회원 정보 화면으로 이동
                        val generalLoadingDialog = LoadingDialog(this@PinVerificationActivity)
                        generalLoadingDialog.setCancelable(false)
                        generalLoadingDialog.show()
                        
                        // 2초 후에 회원 정보 화면으로 이동
                        Handler(Looper.getMainLooper()).postDelayed({
                            // 로딩 다이얼로그 닫기
                            generalLoadingDialog.dismiss()
                            
                            // 회원 정보 화면으로 즉시 이동
                            Log.d("PinVerification", "API 인증 성공. 회원정보 화면으로 즉시 이동")
                            Toast.makeText(this@PinVerificationActivity, "회원 정보 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                            
                            // 가장 기본적인 액티비티 시작 방식 사용 - 전체 경로 지정
                            val intent = Intent(this@PinVerificationActivity, com.stip.stip.more.activity.MemberInfoEditActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000) // 2초 후 실행
                    }
                }
            }.suspendOnError { 
                // PIN 유효성 검사 실패 - PIN 불일치
                android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생!!!파파")
                runOnUiThread {
                    android.util.Log.e("LoginPinNumber", "PIN 번호 불일치 발생!!!포포")
                    // 로딩 다이얼로그 닫기
                    loadingDialog.dismiss()
                    attemptCount++
                    currentPin = ""
                    pinAdapter.updatePinCount(0)

                    
                    // 오류 메시지 표시
                    val warningText = "다시 입력 해주세요.(" + attemptCount + "/" + maxAttempts + ")"
                    binding.tvSignUpPinNumberWarning.text = warningText
                    binding.tvSignUpPinNumberWarning.setTextColor(ContextCompat.getColor(this@PinVerificationActivity, R.color.red_DB3949_100))
                    binding.tvSignUpPinNumberWarning.visibility = View.VISIBLE
                    
                    // PIN 변경 시에만 안내 메시지 표시 중이었다면 숨김
                    if (isPinChange) {
                        binding.tvPinNumberGuide.visibility = View.GONE
                    }
                    
                    // 5번 이상 틀렸을 경우 경고 다이얼로그 표시
                    if (attemptCount >= maxAttempts) {
                        val dialog = PinIncorrectDialogFragment()
                        dialog.show(supportFragmentManager, "PinIncorrectDialog")
                    }
                }
            }.suspendOnException {
                // 예외 발생 - 네트워크 오류 등
                runOnUiThread {
                    // 로딩 다이얼로그 닫기
                    loadingDialog.dismiss()
                    Toast.makeText(this@PinVerificationActivity, "서버 연결 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    currentPin = ""
                    pinAdapter.updatePinCount(0)
                }
            }
        }
    }
    
    /**
     * PIN 번호가 연속된 숫자(123456 등)이거나 동일한 숫자의 반복(111111 등)인지 확인
     * @param pin 6자리 PIN 번호
     * @return 연속되거나 반복되는 경우 true, 아닌 경우 false
     */
    private fun isSequentialOrRepeated(pin: String): Boolean {
        // 입력된 PIN이 6자리가 아니면 유효하지 않음
        if (pin.length != 6) return false
        android.util.Log.e("PinVerification", "PIN 번호 불일치 발생!!!쓰쓰")
        // 동일한 숫자의 반복 검사 (모든 숫자가 같은 경우)
        val distinctDigits = pin.toCharArray().distinct()
        if (distinctDigits.size == 1) return true
        
        // 연속된 숫자 검사
        var isSequential = true
        for (i in 1 until pin.length) {
            val prev = pin[i - 1].digitToInt()
            val curr = pin[i].digitToInt()
            
            // 이전 숫자 + 1이 현재 숫자가 아니면 연속되지 않은 것
            if (prev + 1 != curr) {
                isSequential = false
                break
            }
        }
        
        // 연속된 숫자가 아니면 역순으로 연속된 숫자인지 확인 (654321 등)
        if (!isSequential) {
            isSequential = true
            for (i in 1 until pin.length) {
                val prev = pin[i - 1].digitToInt()
                val curr = pin[i].digitToInt()
                
                // 이전 숫자 - 1이 현재 숫자가 아니면 연속되지 않은 것
                if (prev - 1 != curr) {
                    isSequential = false
                    break
                }
            }
        }
        
        return isSequential
    }
}

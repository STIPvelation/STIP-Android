package com.stip.stip.more.fragment // 실제 패키지 경로로 변경

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.stip.stip.R // 실제 R 경로로 변경 필요
import com.stip.stip.databinding.FragmentMoreMemberEmailEditBinding // 생성된 바인딩 클래스 이름으로 변경
import com.stip.stip.more.viewmodel.MemberInfoEditViewModel

class EmailChangeFragment : Fragment() {

    private var _binding: FragmentMoreMemberEmailEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemberInfoEditViewModel by viewModels()

    private var currentEmail: String? = null
    private var onEmailChangeListener: ((String) -> Unit)? = null

//    private var isWaitingForCode = false
//    private var currentEmail: String? = null
//
//    private var countDownTimer: CountDownTimer? = null
//    private var verificationAttempts = 0
//    private val MAX_ATTEMPTS = 5
//    private val TIMER_DURATION_MS = TimeUnit.MINUTES.toMillis(5) // 5분 타이머

    companion object {
        private const val ARG_CURRENT_EMAIL = "current_email"
        @JvmStatic
        fun newInstance(currentEmail: String) =
            EmailChangeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_EMAIL, currentEmail)
                }
            }
    }

    fun setOnEmailChangeListener(listener: (String) -> Unit) {
        onEmailChangeListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentEmail = it.getString(ARG_CURRENT_EMAIL)
            Log.d("EmailChangeFragment", "Received current email: $currentEmail")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreMemberEmailEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        setupObservers()

        currentEmail?.let { email ->
            if (email.isNotBlank()) {
                binding.etEmail.setText(email)
                binding.etEmail.setSelection(email.length)
                updateButtonState(true)
            }
        }

        showKeyboard(binding.etEmail)
    }

    private fun setupViews() {
        binding.etEmail.hint = getString(R.string.email_hint_empty)
        binding.etEmail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.tvTitle.text = getString(R.string.prompt_enter_email)
        binding.tvInfo.visibility = View.VISIBLE
        binding.btnRequestCode.text = "이메일 변경"
        
        // 인증 관련 뷰들 숨기기
        binding.tvInfoAttempts.visibility = View.GONE
        binding.tvCodeError.visibility = View.GONE
        binding.btnVerifyCode.visibility = View.GONE
        binding.tvTimerInfo.visibility = View.GONE
//        binding.tvInfo.visibility = View.VISIBLE
//        binding.btnRequestCode.visibility = View.VISIBLE
//        updateButtonState(binding.etEmail.text.isNotBlank())
    }


//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//        if (isWaitingForCode && binding.tvCodeError.visibility == View.VISIBLE) {
//            binding.tvCodeError.visibility = View.GONE
//        }
//    }
//    override fun be

    private fun setupListeners() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateButtonState(s.toString().isNotBlank())
//                if (!isWaitingForCode) {
//                    updateButtonState(s.toString().isNotBlank())
//                } else {
//                    updateButtonState(false)
//                    updateVerifyButtonState(s?.length == 4)
//                }
            }
        })

        binding.btnRequestCode.setOnClickListener {
//            if (it.isEnabled && !isWaitingForCode) {
//                val newEmail = binding.etEmail.text.toString().trim()
//
//                // 기본 입력 값 검증
//                if (newEmail == currentEmail) {
//                    Toast.makeText(requireContext(), "현재 이메일과 동일합니다. 변경할 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
//                    Toast.makeText(requireContext(), "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//
//                Log.d("EmailChangeFragment", "Requesting code for email: $newEmail")
//
//                // 실제 API 호출 구현
//                // 임시 UI 비활성화
//                binding.btnRequestCode.isEnabled = false
//                binding.btnRequestCode.text = "요청 중..."
//
//                // 새 이메일 저장
//                arguments?.putString("NEW_EMAIL", newEmail)
//
//                // 실제 API 호출 - 예시
//                // viewLifecycleOwner.lifecycleScope.launch {
//                //     try {
//                //         val repository = MemberRepository()
//                //         val result = repository.requestEmailVerificationCode(newEmail)
//                //
//                //         activity?.runOnUiThread {
//                //             if (result) {
//                //                 // 성공적으로 인증 코드 전송 완료
//                //                 switchToCodeEntryMode()
//                //             } else {
//                //                 // 인증 코드 전송 실패
//                //                 binding.btnRequestCode.isEnabled = true
//                //                 binding.btnRequestCode.text = "인증번호 요청"
//                //                 Toast.makeText(requireContext(), "인증번호 전송에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//                //             }
//                //         }
//                //     } catch (e: Exception) {
//                //         Log.e("EmailChangeFragment", "API 오류: ${e.message}")
//                //         activity?.runOnUiThread {
//                //             binding.btnRequestCode.isEnabled = true
//                //             binding.btnRequestCode.text = "인증번호 요청"
//                //             Toast.makeText(requireContext(), "인증번호 전송에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//                //         }
//                //     }
//                // }
//
//                // 테스트용 임시 지연 구현 - API 완료 후 제거
//                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
//                    binding.btnRequestCode.text = "인증번호 요청"
//                    binding.btnRequestCode.isEnabled = true
//                    // API 연동 테스트용 성공 가정
//                    switchToCodeEntryMode()
//                }, 1000) // 1초 지연
//            }
//        }

            //            private fun verifyCode(code: String) {
//                Log.d("EmailChangeFragment", "Verifying code: $code")
//                hideKeyboard()
//                cancelTimer()
//
//                val newEmail = arguments?.getString("NEW_EMAIL") ?: ""
//                if (newEmail.isBlank()) {
//                    Log.e("EmailChangeFragment", "No new email found")
//                    showVerificationError()
//                    return
//                }
//
//                // API 연동 구현
//                try {
//                    // 실제 구현에서는 viewModel이나 Repository를 통해 호출
//                    // val repository = MemberRepository()
//                    // viewLifecycleOwner.lifecycleScope.launch {
//                    //     try {
//                    //         val result = repository.verifyEmailCode(newEmail, code)
//                    //         if (result) {
//                    //             // 인증 성공
//                    //             activity?.runOnUiThread {
//                    //                 // 변경된 이메일 전달
//                    //                 onEmailChangeListener?.invoke(newEmail)
//                    //                 showEmailChangeSuccessDialog()
//                    //             }
//                    //         } else {
//                    //             // 인증 실패
//                    //             activity?.runOnUiThread {
//                    //                 showVerificationError()
//                    //             }
//                    //         }
//                    //     } catch (e: Exception) {
//                    //         Log.e("EmailChangeFragment", "API error: ${e.message}")
//                    //         activity?.runOnUiThread {
//                    //             showVerificationError()
//                    //         }
//                    //     }
//                    // }
//            }
            if (!it.isEnabled) return@setOnClickListener

            val newEmail = binding.etEmail.text.toString().trim()
            
            // 기본 입력 값 검증
            if (newEmail == currentEmail) {
                Toast.makeText(requireContext(), "현재 이메일과 동일합니다. 변경할 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            private fun switchToCodeEntryMode() {
//                isWaitingForCode = true
//                verificationAttempts = 0
//                binding.tvTitle.text = getString(R.string.prompt_enter_code)
//                binding.etEmail.setText("")
//                binding.etEmail.hint = getString(R.string.code_hint)
//                binding.etEmail.inputType = InputType.TYPE_CLASS_NUMBER
//                binding.etEmail.filters = arrayOf(InputFilter.LengthFilter(4))
//                binding.tvInfoAttempts.visibility = View.VISIBLE
//                binding.tvCodeError.visibility = View.GONE
//                binding.btnVerifyCode.visibility = View.VISIBLE
//                binding.tvTimerInfo.visibility = View.VISIBLE
//                binding.tvInfo.visibility = View.GONE
//                binding.btnRequestCode.visibility = View.GONE
//                updateButtonState(false)
//                updateVerifyButtonState(false)
//                hideKeyboard()
//                binding.etEmail.requestFocus()
//                startTimer()
//            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(requireContext(), "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 버튼 비활성화 및 로딩 표시
            binding.btnRequestCode.isEnabled = false
            binding.btnRequestCode.text = "변경 중..."
            
            // 이메일 변경 API 호출
            viewModel.updateEmail(newEmail)
        }
    }

    private fun setupObservers() {
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            // 성공 시 이메일 변경 콜백 호출 및 화면 종료
            binding.etEmail.text.toString().trim().let { newEmail ->
                onEmailChangeListener?.invoke(newEmail)
            }
            parentFragmentManager.popBackStack()
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            // 에러 발생 시 버튼 상태 복구
            binding.btnRequestCode.isEnabled = true
            binding.btnRequestCode.text = "이메일 변경"
        }
    }

    private fun updateButtonState(enabled: Boolean) {
        binding.btnRequestCode.isEnabled = enabled
    }

//    private fun hideKeyboard() {
//        context ?: return
//        val imm =
//            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//        val currentFocus = activity?.currentFocus ?: view
//        currentFocus?.let {
//            imm?.hideSoftInputFromWindow(it.windowToken, 0)
//        }
//    }
    private fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
//    // 이메일 변경 리스너 인터페이스
//    private var onEmailChangeListener: ((String) -> Unit)? = null
//
//    // 이메일 변경 리스너 설정 메서드
//    fun setOnEmailChangeListener(listener: (String) -> Unit) {
//        onEmailChangeListener = listener
//    }

    override fun onDestroyView() {
        super.onDestroyView()
//        cancelTimer() // Fragment 소멸 시 타이머 확실히 취소
        _binding = null
    }
}
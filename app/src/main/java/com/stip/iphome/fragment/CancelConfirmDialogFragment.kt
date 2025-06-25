package com.stip.stip.iphome.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.stip.stip.R
import com.stip.stip.databinding.DialogCancelConfirmBinding // ViewBinding Import 확인

class CancelConfirmDialogFragment : DialogFragment() {

    private var _binding: DialogCancelConfirmBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "CancelConfirmDialog"
        const val REQUEST_KEY = "cancel_confirm_request"
        const val RESULT_KEY_CONFIRMED = "cancel_confirm_result"

        private const val ARG_TITLE_RES_ID = "title_res_id"
        private const val ARG_MESSAGE = "message"

        fun newInstance(titleResId: Int, message: String): CancelConfirmDialogFragment {
            return CancelConfirmDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TITLE_RES_ID, titleResId)
                    putString(ARG_MESSAGE, message)
                }
            }
        }
    }

    // --- ▼▼▼ onCreateView 사용 방식으로 변경 ▼▼▼ ---
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCancelConfirmBinding.inflate(inflater, container, false)

        // 다이얼로그 기본 배경 및 타이틀 제거 (onCreateView 에서도 설정 가능)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return binding.root
    }
    // --- ▲▲▲ onCreateView 사용 방식으로 변경 ▲▲▲ ---

    // onCreateDialog 메서드는 제거합니다.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleResId = arguments?.getInt(ARG_TITLE_RES_ID) ?: R.string.dialog_title_cancel_order_confirm
        val message = arguments?.getString(ARG_MESSAGE) ?: ""

        // ViewBinding을 통해 뷰 ID 접근
        binding.dialogLogoutTitle.text = getString(titleResId)
        binding.dialogLogoutMessage.text = message

        binding.dialogLogoutButtonConfirm.text = getString(R.string.dialog_button_confirm)
        binding.dialogLogoutButtonConfirm.setOnClickListener {
            parentFragmentManager.setFragmentResult(REQUEST_KEY, Bundle().apply {
                putBoolean(RESULT_KEY_CONFIRMED, true)
            })
            dismiss()
        }

        binding.dialogLogoutButtonCancel.text = getString(R.string.dialog_button_cancel)
        binding.dialogLogoutButtonCancel.setOnClickListener {
            parentFragmentManager.setFragmentResult(REQUEST_KEY, Bundle().apply {
                putBoolean(RESULT_KEY_CONFIRMED, false)
            })
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그 크기 조절 (레이아웃 파일의 layout_margin 과 함께 적용됨)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
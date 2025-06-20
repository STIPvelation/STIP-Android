package com.stip.stip.signup.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.stip.stip.R

class AddressBottomSheet(
    private val zipCode: String,
    private val address: String,
    private val confirmClick: ((String) -> Unit)
): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.bottom_dialog_kyc_address, container, false)
        val tvZipCode = view.findViewById<AppCompatTextView>(R.id.tv_sign_up_kyc_address_zip_code)
        val tvAddress = view.findViewById<AppCompatTextView>(R.id.tv_sign_up_kyc_address)
        val etDetailAddress = view.findViewById<AppCompatEditText>(R.id.et_sign_up_kyc_search_address_detail)
        val btnConfirm = view.findViewById<AppCompatTextView>(R.id.btn_confirm)
        var detailAddress: String

        tvZipCode.text = zipCode
        tvAddress.text = address
        etDetailAddress.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrBlank()) {
                btnConfirm.isEnabled = false
            } else {
                btnConfirm.isEnabled = true
            }
        }
        btnConfirm.setOnClickListener {
            detailAddress = etDetailAddress.text.toString()
            confirmClick.invoke(detailAddress)
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onStop() {
        super.onStop()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<AppCompatImageView>(R.id.iv_close)?.setOnClickListener {
            dismiss()
        }
    }
}
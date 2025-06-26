package com.stip.stip.ipasset.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.stip.stip.databinding.FragmentTransactionDialogBinding

class TransactionDialogFragment : BaseDialogFragment<FragmentTransactionDialogBinding>() {
    private var onClickListener: OnClickListener? = null
    private var onDismissListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false

        parentFragment?.let {
            if (it is OnClickListener) {
                onClickListener = it
            }
        }
    }

    override fun inflate(inflater: LayoutInflater): FragmentTransactionDialogBinding {
        return FragmentTransactionDialogBinding.inflate(inflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        viewBinding.title.text = arguments?.getString(KEY_TITLE)
        viewBinding.message.text = arguments?.getString(KEY_MESSAGE)
        viewBinding.confirmButton.setOnClickListener {
            onClickListener?.onConfirmButtonClick(this) ?: dismiss()
        }

        return dialog
    }
    
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
    
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    interface OnClickListener {
        fun onConfirmButtonClick(dialogFragment: DialogFragment)
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"

        fun newInstance(
            title: String,
            message: String
        ): TransactionDialogFragment {
            return TransactionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TITLE, title)
                    putString(KEY_MESSAGE, message)
                }
            }
        }
    }
}

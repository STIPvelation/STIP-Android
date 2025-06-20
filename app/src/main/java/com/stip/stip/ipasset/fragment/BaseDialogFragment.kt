package com.stip.stip.ipasset.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {
    private var _viewBinding: VB? = null
    protected val viewBinding: VB
        get() = _viewBinding!!

    abstract fun inflate(inflater: LayoutInflater): VB

    @CallSuper
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBinding = inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .create()
    }

    @CallSuper
    override fun onDestroyView() {
        _viewBinding = null

        super.onDestroyView()
    }
}

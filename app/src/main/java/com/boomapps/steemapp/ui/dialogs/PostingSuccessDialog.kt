/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.dialogs

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.boomapps.steemapp.R

class PostingSuccessDialog : DialogFragment() {

    private lateinit var onDialogDismissListener: OnDialogDismissListener

    interface OnDialogDismissListener {
        fun onDismiss()
    }

    fun setOnDismissListener(l: OnDialogDismissListener) {
        this.onDialogDismissListener = l
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_posting_succes, null)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        isCancelable = false
        return v
    }

    override fun onResume() {
        super.onResume()
        view?.postDelayed({
            dialog?.dismiss()
            onDialogDismissListener.onDismiss()
        }, 3000)
    }
}

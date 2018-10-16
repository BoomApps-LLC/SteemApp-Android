/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.dialogs

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import com.boomapps.steemapp.R

/**
 * Created on 26.08.2016.
 */
class ProgressFragmentDialog : DialogFragment() {

    private var textMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textMessage = arguments!!.getString(KEY_PROGRESS_MESSAGE, null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = inflater.inflate(R.layout.dialog_progress, null)
        if (textMessage != null)
            (view.findViewById<View>(R.id.progressMessage) as TextView).text = textMessage
        (view.findViewById<View>(R.id.progressView) as ProgressBar).indeterminateDrawable.setColorFilter(ContextCompat.getColor(inflater.context, R.color.colorAccent),
                android.graphics.PorterDuff.Mode.SRC_IN)
        isCancelable = false
        return view
    }

    companion object {

        val TAG = ProgressFragmentDialog::class.java.simpleName

        private val KEY_PROGRESS_MESSAGE = "progress_message"

        fun newInstance(text: String?): ProgressFragmentDialog {
            val args = Bundle()
            if (text != null) args.putString(KEY_PROGRESS_MESSAGE, text)
            val fragment = ProgressFragmentDialog()
            fragment.arguments = args
            return fragment
        }
    }

}

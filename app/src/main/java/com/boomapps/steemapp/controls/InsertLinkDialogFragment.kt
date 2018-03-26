package com.boomapps.steemapp.controls

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.WindowManager
import com.boomapps.steemapp.R

/**
 * Created by Vitali Grechikha on 05.02.2018.
 */
class InsertLinkDialogFragment : AppCompatDialogFragment() {

    private var listener: OnInsertClickListener? = null

    companion object {
        fun newInstance(): InsertLinkDialogFragment {
            return InsertLinkDialogFragment()
        }
    }

    fun setOnInsertClickListener(listener: OnInsertClickListener) {
        this.listener = listener
    }

    override fun show(manager: FragmentManager?, tag: String?) {
//        super.show(manager, tag)
        if (manager?.findFragmentByTag(tag) == null) {
            super.show(manager, tag)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (dialog.window != null) {
            dialog.window!!.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_insert_link, null)
        val textToDisplayEditText = view.findViewById<TextInputEditText>(R.id.text_to_display)
        val linkToEditText = view.findViewById<TextInputEditText>(R.id.link_to)

        val dialog = AlertDialog.Builder(activity as Context)
        dialog.setTitle("Add link")
        dialog.setView(view)
        dialog.setPositiveButton("ADD", {_, _ ->
            val title = textToDisplayEditText.getText().toString().trim()
            val url = linkToEditText.getText().toString().trim()

            if (listener != null) {
                listener!!.onInsertClick(title, url)
            }
        })
        dialog.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })

        return dialog.create()
    }

    interface OnInsertClickListener {
        fun onInsertClick(title: String, url: String)
    }

}
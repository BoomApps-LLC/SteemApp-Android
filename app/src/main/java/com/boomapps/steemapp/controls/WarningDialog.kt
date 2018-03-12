package com.boomapps.steemapp.controls

import android.content.Context
import android.support.v7.app.AlertDialog
import com.boomapps.steemapp.R

/**
 * Created by vgrechikha on 09.02.2018.
 */
class WarningDialog {

    interface OnPositiveClickListener {
        fun onClick()
    }

    companion object {
        fun getInstance(): WarningDialog {
            return WarningDialog()
        }
    }

    /**
     * Show full custom dialog
     * @param context context
     * @param title title for dialog; if it is empty dialog won't show title
     * @param message error message for dialog
     * @param positive title for positive button
     * @param negative title for negative button
     * @param listener own callback for positive button
     */
    fun showSpecial(context: Context, title: String?, message: String, positive: String, negative: String?, listener: OnPositiveClickListener?) {
        getDialog(context, title, message, positive, negative, listener).show()
    }

    fun showErrorWithRetry(context: Context, message: String, listener: OnPositiveClickListener) {
        getDialog(
                context,
                null,
                message,
                context.resources.getString(R.string.dialog_error_retry_button),
                context.resources.getString(R.string.dialog_error_negative_button),
                listener).show()
    }


    private fun getDialog(context: Context, title: String?, message: String, positive: String, negative: String?, listener: OnPositiveClickListener?): AlertDialog {
        val builder = AlertDialog.Builder(context, R.style.AppDialogTheme)
        builder.setMessage(message)
        if (!title.isNullOrEmpty())
            builder.setTitle(title)
        builder.setPositiveButton(positive) { dialogInterface, _ ->
            listener?.onClick()
            dialogInterface.dismiss()
        }
        if (!negative.isNullOrEmpty()) {
            builder.setNegativeButton(negative) { dialogInterface, _ -> dialogInterface.dismiss() }
        }

        val dialog = builder.create()
        dialog.setCancelable(true)
        return dialog
    }
}
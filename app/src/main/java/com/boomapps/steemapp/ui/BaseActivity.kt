/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.boomapps.steemapp.ui.dialogs.ProgressFragmentDialog
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.dialogs.WarningDialog

/**
 * Created by vgrechikha on 29.01.2018.
 */
open class BaseActivity : AppCompatActivity() {

    fun showProgress(message: String) {
        if (isProgressShowed()) return
        ProgressFragmentDialog.newInstance(message).show(supportFragmentManager, ProgressFragmentDialog.TAG)
    }

    fun showProgress(resId: Int) {
        showProgress(getString(resId))
    }

    private fun isProgressShowed(): Boolean {
        val f = supportFragmentManager.findFragmentByTag(ProgressFragmentDialog.TAG)
        return f != null && (f.isVisible || f.isAdded)
    }

    fun dismissProgress() {
        val fragment = supportFragmentManager.findFragmentByTag(ProgressFragmentDialog.TAG)
        if (fragment != null) {
            (fragment as ProgressFragmentDialog).dismiss()
        }
    }

//    WARNINGS

    protected fun showSimpleWarning(message: String) {
        WarningDialog.getInstance().showSpecial(this, null, message, getString(R.string.dialog_positive_button), null, null)
    }

    protected fun showSimpleWarning(message: String, listener: WarningDialog.OnPositiveClickListener) {
        WarningDialog.getInstance().showSpecial(this, null, message, getString(R.string.dialog_positive_button), null, listener)
    }

    protected fun showExtendWarning(title: String?, message: String, buttonOk: String?, buttonCancel: String?, listener: WarningDialog.OnPositiveClickListener) {
        lateinit var finalButtonOk: String
        lateinit var finalButtonCancel: String
        if (buttonOk.isNullOrEmpty()) {
            finalButtonOk = getString(R.string.dialog_common_ok_button)
        } else {
            finalButtonOk = buttonOk!!
        }
        if (buttonCancel.isNullOrEmpty()) {
            finalButtonCancel = getString(R.string.dialog_common_cancel_button)
        } else {
            finalButtonCancel = buttonCancel!!
        }
        WarningDialog.getInstance().showSpecial(this, title, message, finalButtonOk, finalButtonCancel, listener)
    }

}


fun EditText.showKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard(context : Context){
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.getWindowToken(), 0)
}
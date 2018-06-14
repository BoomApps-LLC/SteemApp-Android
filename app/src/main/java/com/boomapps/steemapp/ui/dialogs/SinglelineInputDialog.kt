package com.boomapps.steemapp.ui.dialogs

import android.app.DialogFragment
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.Utils

class SinglelineInputDialog : DialogFragment(), View.OnClickListener {

    private var onDataChangeListener: OnDataChangeListener? = null
    private var startValue: String? = null
    private var isOnlyNumbers: Boolean = false
    private var limit = 0
    private var etInput: TextInputLayout? = null
    private var titleText: String? = null
    private var btnConfirm: View? = null
    private var btnCancel: View? = null

    interface OnDataChangeListener {
        fun onDataChange(newValue: String)
    }

    private fun setOnDataChangeListener(l: OnDataChangeListener) {
        this.onDataChangeListener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments ?: return
        startValue = bundle.getString(BUNDLE_ARG_VALUE, "")
        isOnlyNumbers = bundle.getBoolean(BUNDLE_ARG_ONLY_NUMBER)
        titleText = bundle.getString(BUNDLE_ARG_TITLE)
        limit = bundle.getInt(BUNDLE_ARG_LIMIT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_singleline_input, null)
        if(dialog != null){
            dialog.setTitle(titleText)
        }
        etInput = v.findViewById(R.id.dialogSingleLineInput_etInput)
        etInput?.editText?.setText(startValue)
        if (isOnlyNumbers)
            etInput?.editText?.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        if (limit > 0) {
            Utils.get().setInputMaxLength(etInput?.editText, limit)
            etInput?.counterMaxLength = limit
        }
        btnConfirm = v.findViewById(R.id.dialogSingleLineInput_btnConfirm)
        btnConfirm?.setOnClickListener {
            if (onDataChangeListener != null) {
                onDataChangeListener?.onDataChange(etInput?.editText?.text.toString())
                dialog.dismiss()
            }
        }
        btnCancel = v.findViewById(R.id.dialogSingleLineInput_btnCancel)
        btnCancel?.setOnClickListener { dialog.dismiss() }
        return v
    }

    override fun onClick(v: View) {}

    companion object {

        private val BUNDLE_ARG_TITLE = "title"
        private val BUNDLE_ARG_VALUE = "value"
        private val BUNDLE_ARG_ONLY_NUMBER = "only_number"
        private val BUNDLE_ARG_LIMIT = "limit"

        fun newInstance(title: String, startValue: String?, isOnlyNumbers: Boolean, listener: OnDataChangeListener): SinglelineInputDialog {
            return newInstance(title, startValue, isOnlyNumbers, 0, listener)
        }

        fun newInstance(title: String, startValue: String?, isOnlyNumbers: Boolean, limit: Int, listener: OnDataChangeListener): SinglelineInputDialog {
            val dialog = SinglelineInputDialog()
            dialog.setOnDataChangeListener(listener)
            val bundle = Bundle()
            bundle.putString(BUNDLE_ARG_TITLE, title)
            bundle.putString(BUNDLE_ARG_VALUE, startValue ?: "")
            bundle.putBoolean(BUNDLE_ARG_ONLY_NUMBER, isOnlyNumbers)
            bundle.putInt(BUNDLE_ARG_LIMIT, limit)
            dialog.arguments = bundle
            return dialog
        }
    }


}

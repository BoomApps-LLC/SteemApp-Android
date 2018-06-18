/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.boomapps.steemapp.R

class VotePostDialog : DialogFragment() {

    interface OnVotePercentSelectListener {
        fun onSelect(value: Int)
    }

    private var value = 50
    private lateinit var tvValue: TextView
    private var valueSelectListener: OnVotePercentSelectListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.dialog_vote_post, null)
        if (dialog != null) {
            dialog.setTitle("Vote")
        }
        tvValue = v.findViewById(R.id.dialogVotePost_tvValue)
        v.findViewById<SeekBar>(R.id.dialogVotePost_sbPercent).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value = progress
                // TODO use string resource instead concatenation
                tvValue.text = progress.toString() + "%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        v.findViewById<AppCompatButton>(R.id.dialogVotePost_btnConfirm).setOnClickListener({
            valueSelectListener?.onSelect(value)
            dialog.dismiss()
        })

        v.findViewById<AppCompatButton>(R.id.dialogVotePost_btnCancel).setOnClickListener({
            dialog.dismiss()
        })

        return v
    }


    fun setOnVotePercentSelectListener(listener: OnVotePercentSelectListener) {
        valueSelectListener = listener
    }


    companion object {

        val TAG = VotePostDialog::class.java.simpleName

        fun newInstance(listener: OnVotePercentSelectListener): VotePostDialog {
            val fragment = VotePostDialog()
            fragment.setOnVotePercentSelectListener(listener)
            return fragment
        }
    }

}
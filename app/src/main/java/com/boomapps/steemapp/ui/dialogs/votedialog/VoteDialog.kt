package com.boomapps.steemapp.ui.dialogs.votedialog

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.boomapps.steemapp.R
import kotlinx.android.synthetic.main.dialog_vote.view.*

class VoteDialog : DialogFragment() {

    interface OnVoteInteractListener {
        fun onCloseClick()

        fun onVoteClick()
    }

    companion object {

        val TAG = "vote_dialog"

        fun newInstance(listener: OnVoteInteractListener) : VoteDialog {
            val dialog = VoteDialog()
            dialog.setOnVoteInteractListener(listener)
            return dialog
        }
    }

    private lateinit var onVoteInteractListener: OnVoteInteractListener

    fun setOnVoteInteractListener(listener: OnVoteInteractListener) {
        onVoteInteractListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //setup dialog view
        val view = inflater.inflate(R.layout.dialog_vote, container, false)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        isCancelable = false
        view.dVote_ivClose.setOnClickListener({
            dialog.dismiss()
            onVoteInteractListener.onCloseClick()
        })
        view.dVote_btnVote.setOnClickListener({
            dialog.dismiss()
            onVoteInteractListener.onVoteClick()
        })
        return view
    }

}
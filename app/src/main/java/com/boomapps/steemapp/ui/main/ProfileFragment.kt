/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.main


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.ui.ViewState
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import kotlinx.android.synthetic.main.fragment_profile_new.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*


class ProfileFragment : Fragment() {

    private lateinit var viewModelShared: MainViewModel
    private var balanceValue: Array<String> = arrayOf("0", "00")
    private var balanceSymbol = "$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelShared = ViewModelProviders.of(activity as AppCompatActivity).get(MainViewModel::class.java)
        viewModelShared.state.observe(this, object : Observer<ViewState> {
            override fun onChanged(t: ViewState?) {
                when (t) {
                    ViewState.SUCCESS_RESULT -> {
                        viewModelShared.state.value = ViewState.COMMON
                        viewModelShared.stringError = ""
                        viewModelShared.stringSuccess = ""
                    }
                    ViewState.FAULT_RESULT -> {
                        viewModelShared.state.value = ViewState.COMMON
                        viewModelShared.stringError = ""
                    }
                    ViewState.COMMON -> {

                    }
                }
            }
        })
//        viewModelShared.getUserProfile().observe(this, Observer<UserData> { data ->
//            if (data != null) {
//                showProfileData(data)
//            }
//        })

        viewModelShared.getBalance().observe(this, object : Observer<Balance> {
            override fun onChanged(data: Balance?) {
                if (data != null) {
                    val asString: String = data.fullBalance.toString()
                    var intVal = asString.substringBefore(".")
                    if (intVal.toInt() < 0) {
                        intVal = "0"
                    }
                    val after = asString.substringAfter(".")
                    balanceValue = arrayOf(
                            intVal,
                            if (after.isEmpty()) {
                                "0"
                            } else {
                                after.subSequence(0, Math.min(2, after.length))
                            }.toString()
                    )

                    myBalanceValue.text = getString(R.string.balance_template, balanceSymbol, balanceValue[0], balanceValue[1])
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_new, container, false)
        view.profileSignOutControl.setOnClickListener({
            val context = view.context
            WarningDialog.getInstance().showSpecial(
                    context = context,
                    title = null,
                    message = getString(R.string.signout_confirm_dialog_msg),
                    positive = context.getString(R.string.button_confirm),
                    negative = context.getString(R.string.button_cancel),
                    listener = object : WarningDialog.OnPositiveClickListener {
                        override fun onClick() {
                            viewModelShared.signOut()
                            (activity as MainActivity).goToSignInScreen()
                        }
                    }
            )

        })

        view.profileFeedbackControl.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:"); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@steemitapp.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            if (intent.resolveActivity(activity?.packageManager) != null) {
                startActivity(intent)
            }
        }
        view.profileShareControl.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out SteemApp at: https://play.google.com/store/apps/details?id=com.boomapps.steemapp")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        view.profileRateControl.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("market://details?id=" + SteemApplication.instance.packageName)
            startActivity(i)
        }
        view.poweredBy.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://steemit.com/@adsactly")
            startActivity(i)
        }
        return view
    }

//    fun showProfileData(uData: UserData?) {
//        if (uData != null) {
//            if (!uData.userName.isNullOrEmpty()) {
//                profileFullName.setText(uData.userName)
//            } else {
//                profileFullName.setText(uData.nickname)
//            }
//            if (uData.photoUrl.isNullOrEmpty()) {
//                return
//            }
//            Glide.with(this@ProfileFragment)
//                    .load(uData.photoUrl)
//                    .apply(circleCropTransform())
//                    .into(profileAvatar)
//        }
//    }

    override fun onResume() {
        super.onResume()
//        showProfileData(viewModelShared.getUserProfile().value)
    }

    companion object {
        fun newInstance(): ProfileFragment {
            val fragment = ProfileFragment()
            return fragment
        }
    }
}// Required empty public constructor

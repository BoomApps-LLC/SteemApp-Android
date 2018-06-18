/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.signin

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.ui.BaseActivity
import com.boomapps.steemapp.ui.ViewState
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import com.boomapps.steemapp.ui.help.HelpActivity
import com.boomapps.steemapp.ui.main.MainActivity
import com.boomapps.steemapp.ui.qrreader.BarcodeReadActivity
import kotlinx.android.synthetic.main.activity_signin.*

/**
 * Created by Vitali Grechikha on 24.01.2018.
 */
class SignInActivity : BaseActivity() {

    private lateinit var viewModel: SignInViewModel

    private val BARCODE_READER_ACTIVITY = 5646

    private val PERMISSION_REQUEST_CAMERA = 3546


    fun getErrorString(defValueId: Int): String {
        return if (viewModel.stringError.isNullOrBlank()) {
            getString(defValueId)
        } else {
            viewModel.stringError
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        viewModel = ViewModelProviders.of(this).get(SignInViewModel::class.java)
        viewModel.state.observe(this, object : Observer<ViewState> {
            override fun onChanged(t: ViewState?) {
                when (t) {
                    ViewState.COMMON -> {
                        dismissProgress()
                    }
                    ViewState.PROGRESS -> {
                        showProgress("Sign In ...")
                    }

                    ViewState.SUCCESS_RESULT -> {
                        viewModel.state.value = ViewState.COMMON
                        viewModel.stringError = ""
                        viewModel.stringSuccess = ""
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        startActivity(intent)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        finish()
                    }
                    ViewState.FAULT_RESULT -> {
                        viewModel.state.value = ViewState.COMMON
                        val errorMessage = if (viewModel.loginResult == SignInViewModel.LOGIN_ERROR_BAD_DATA) {
                            getErrorString(R.string.error_message_bad_user_data)
                        } else {
                            getErrorString(R.string.error_message_network_connection_fault)
                        }
                        WarningDialog.getInstance().showSpecial(context =
                        this@SignInActivity,
                                title = getString(R.string.error_title_login_error),
                                message = errorMessage,
                                positive = getString(R.string.button_ok),
                                negative = null,
                                listener = null)
                        viewModel.loginResult = -1
                        viewModel.stringError = ""
                    }
                }
            }
        })
        (sigInInputName as EditText).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    viewModel.nickname = s.toString()
                }
                setLoginButtonState()
            }
        })
        (signInInputPostingKey as EditText).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    viewModel.postingKey = s.toString()
                }
                setLoginButtonState()
            }
        })



        btnLogin.setOnClickListener({ _ ->
            viewModel.login()
        })

        btnSignUp.setOnClickListener({
            val webpage = Uri.parse("https://steemit.com/pick_account")
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
        })

        val sr = RepositoryProvider.getPreferencesRepository()
        if (sr.isFirstLaunch()) {
            signInInputPostingKey.onFocusChangeListener = object : View.OnFocusChangeListener {
                override fun onFocusChange(v: View?, hasFocus: Boolean) {
                    if (hasFocus) {
                        signInInputPostingKey.onFocusChangeListener = null
                        sr.setFirstLaunchState(false)
                        openLocalHelpScreen()
                    }
                }
            }
        }

        qreaderStartButton.setOnClickListener({
            openBarCodeReadScreen()
        })

        signInInfo.setOnClickListener({
            openLocalHelpScreen()
            RepositoryProvider.getPreferencesRepository().setFirstLaunchState(false)
        })
    }

    private fun openBarCodeReadScreen() {
        if (!hasCameraPermissions()) {
            requestCameraPermission()
            return
        }
        startActivityForResult(Intent(this, BarcodeReadActivity::class.java), BARCODE_READER_ACTIVITY)
    }

    private fun openLocalHelpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun hasCameraPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openBarCodeReadScreen()
                } else {
                    Toast.makeText(this@SignInActivity, "You have to input POSTING KEY manual", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            var result = data?.getStringExtra("POSTING_KEY")
            if (result != null && result.isNotBlank()) {
                (signInInputPostingKey as EditText).setText(result)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        sigInInputName.setText(viewModel.nickname)
        signInInputPostingKey.setText(viewModel.postingKey)
    }

    fun setLoginButtonState() {
        // TODO uncomment after testing
        if (viewModel.nickname.isNotEmpty()){// && viewModel.postingKey.isNotEmpty() && viewModel.postingKey.length >= 40) {
            btnLogin.apply {
                setTextColor(ContextCompat.getColor(this@SignInActivity, R.color.green_active))
                isEnabled = true
                isClickable = true
            }
        } else {
            btnLogin.apply {
                btnLogin.setTextColor(ContextCompat.getColor(this@SignInActivity, R.color.green_filled))
                isEnabled = false
                isClickable = false
            }
        }
    }
}


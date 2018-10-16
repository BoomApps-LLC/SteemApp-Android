/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.splash

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import com.boomapps.steemapp.ui.main.MainActivity
import com.boomapps.steemapp.ui.signin.SignInActivity

/**
 * Created by Vitali Grechikha on 24.01.2018.
 */
class SplashActivity : AppCompatActivity() {


    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        viewModel.getLoginState().observe(this, Observer<SplashViewModel.LoginState> { state ->
            when (state) {
                SplashViewModel.LoginState.NO_NICK -> {
                    goToSignInScreen()
                }
                SplashViewModel.LoginState.LOGGED -> {
                    goToMainScreen()

                }
                SplashViewModel.LoginState.LOGGED_WITHOUT_BALANCE -> {
                    goToMainScreen(true)
                }
                SplashViewModel.LoginState.NO_EXT_DATA -> {
                    showNetworkErrorMessage()
                }
                else -> {

                }
            }
        })
    }

    private fun showNetworkErrorMessage() {
        WarningDialog.getInstance().showSpecial(this, null, "Connection error.\nTry to launch application later", "Ok", null, object : WarningDialog.OnPositiveClickListener {
            override fun onClick() {
                this@SplashActivity.finish()
            }
        })
    }

    private fun goToMainScreen(updateData: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_UPDATE_DATA, updateData)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun goToSignInScreen() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

}
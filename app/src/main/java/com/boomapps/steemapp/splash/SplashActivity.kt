package com.boomapps.steemapp.splash

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.boomapps.steemapp.R
import com.boomapps.steemapp.main.MainActivity
import com.boomapps.steemapp.signin.SignInActivity

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
                    goToNextScreen(SignInActivity::class.java)
                }
                SplashViewModel.LoginState.LOGGED, SplashViewModel.LoginState.NO_EXT_DATA -> {
                    goToNextScreen(MainActivity::class.java)
                }
                else -> {
                    // to do nothing
                }
            }
        })
    }

    fun goToNextScreen(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        finish()
    }
}
package com.boomapps.steemapp.splash

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.boomapps.steemapp.R
import com.boomapps.steemapp.controls.WarningDialog
import com.boomapps.steemapp.main.MainActivity
import com.boomapps.steemapp.repository.RepositoryProvider
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
        // TODO remove after testing
//        if (BuidConfig.TEST_VOTE) {
//            val repo = RepositoryProvider.instance.getSharedRepository()
//            repo.saveSuccessfulPostingNumber(0)
//            repo.saveVotingState(true)
//        }
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
        startActivity(intent)
        finish()
    }

    private fun goToSignInScreen() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

}
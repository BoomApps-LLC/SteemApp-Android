package com.boomapps.steemapp.ui.splash

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.steem.SteemWorker
import com.boomapps.steemapp.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by vgrechikha on 30.01.2018.
 */
class SplashViewModel : BaseViewModel() {

    enum class LoginState {
        NOT_LOGGED,
        LOGGED,
        LOGGED_WITHOUT_BALANCE,
        NO_NICK,
        NO_EXT_DATA
    }

    var loginState: MutableLiveData<LoginState> = MutableLiveData()

    init {
        loginState.value = LoginState.NOT_LOGGED
    }

    fun getLoginState(): LiveData<LoginState> {
        loginState.value = if (RepositoryProvider.getPreferencesRepository().isUserLogged() || !SteemWorker().isLogged()) {
            LoginState.NOT_LOGGED
        } else {
            LoginState.LOGGED
        }
        if (loginState.value == LoginState.NOT_LOGGED) {
            login()
        }
        return loginState
    }

    private fun login() {
        Log.d("SplashViewModel", "login()")
        val userData = RepositoryProvider.getPreferencesRepository().loadUserData()
        if (userData.nickname.isNullOrEmpty()) {
            loginState.value = LoginState.NO_NICK
            return
        }
        Log.d("SplashViewModel", "userData{${userData.nickname}, ${userData.postKey}}")
        Observable.fromCallable {
            return@fromCallable RepositoryProvider.getSteemRepository().login(userData.nickname!!, userData.postKey)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.d("SplashViewModel", "doOnError")
                    stringError = it.localizedMessage
                    loginState.value = LoginState.NO_NICK
                }
                .doOnNext {
                    Log.d("SplashViewModel", "doOnNext")
                    if (!it.result) {
                        loginState.value = LoginState.NO_EXT_DATA

                    }
                    Log.d("SignInViewModel", "doOnNext")
                }
                .doOnComplete {
                    Log.d("SplashViewModel", "doOnComplete")
                    if (loginState.value != LoginState.NO_EXT_DATA) {
                        loadFullAdditionalData(userData.nickname!!)
                    }
                }
                .subscribe()
    }

    private fun loadFullAdditionalData(nick: String) {
        if (RepositoryProvider.getPreferencesRepository().loadUserData().userName != null) {
            loginState.value = LoginState.LOGGED_WITHOUT_BALANCE;
            return
        }
        RepositoryProvider.getNetworkRepository().loadFullStartData(nick, object : NetworkRepository.OnRequestFinishCallback {

            override fun onSuccessRequestFinish() {
                loginState.value = LoginState.LOGGED
            }

            override fun onFailureRequestFinish(throwable: Throwable) {
                stringError = if (throwable.localizedMessage != null) {
                    throwable.localizedMessage
                } else {
                    throwable.message ?: "empty error"
                }
                loginState.value = LoginState.NO_EXT_DATA
            }
        })
    }

}
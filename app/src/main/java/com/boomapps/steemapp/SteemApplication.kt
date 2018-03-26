package com.boomapps.steemapp

import android.app.Application
import android.content.Context
import com.boomapps.steemapp.repository.SteemWorker
import com.boomapps.steemapp.logging.CrashReportingTree
import timber.log.Timber
import timber.log.Timber.DebugTree



/**
 * Created by vgrechikha on 22.01.2018.
 */


class SteemApplication : Application() {

    companion object {

        var logged: Boolean = false
        var userData: UserData = UserData(null, null, null, null)
        lateinit var instance: SteemApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        val preferences = getSharedPreferences("steem_shares", Context.MODE_PRIVATE)
        logged = preferences.getBoolean("login_state", false)
    }

    private var steemWorker: SteemWorker? = null

    fun saveLoginState(state: Boolean) {
        val preferences = getSharedPreferences("steem_shares", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("login_state", state).apply()
    }

    fun saveUserName(name: String) {
        val preferences = getSharedPreferences("steem_shares", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("username", name).apply()
    }

    fun getUserName(): String {
        val preferences = getSharedPreferences("steem_shares", Context.MODE_PRIVATE)
        return preferences.getString("username", "")
    }

}


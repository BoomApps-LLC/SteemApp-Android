package com.boomapps.steemapp

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.boomapps.steemapp.repository.steem.SteemWorker
import com.boomapps.steemapp.logging.CrashReportingTree
import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.repository.db.AppDatabase
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * Created by vgrechikha on 22.01.2018.
 */


class SteemApplication : Application() {

    companion object {

        var logged: Boolean = false
        var userData: UserData = UserData(null, null, null, null)
        lateinit var database: AppDatabase
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
        database = Room.databaseBuilder(this, AppDatabase::class.java, "steem_app_db").build()
    }


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


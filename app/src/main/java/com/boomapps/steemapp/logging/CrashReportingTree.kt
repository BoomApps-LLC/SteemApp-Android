/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.logging

import android.util.Log
import com.google.firebase.crash.FirebaseCrash
import timber.log.Timber

/**
 * Created by Anatole Salanevich on 18.03.2018.
 */
class CrashReportingTree: Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        FirebaseCrash.logcat(priority, tag, message)
        if (t != null) {
            FirebaseCrash.report(t)
        }
    }

}
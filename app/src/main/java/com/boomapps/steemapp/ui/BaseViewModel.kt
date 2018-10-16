/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by vgrechikha on 29.01.2018.
 */
open class BaseViewModel : ViewModel() {

    var state: MutableLiveData<ViewState> = MutableLiveData()
    var stringError: String = ""
    var stringSuccess: String = ""

    fun viewStateProceeded(){
        state.value = ViewState.COMMON
    }
}
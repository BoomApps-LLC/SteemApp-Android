package com.boomapps.steemapp

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 * Created by vgrechikha on 29.01.2018.
 */
open class BaseViewModel : ViewModel() {

    var state: MutableLiveData<ViewState> = MutableLiveData()
    var stringError: String = ""
    var stringSuccess: String = ""

}
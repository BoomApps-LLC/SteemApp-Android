/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.feed

import java.text.SimpleDateFormat
import java.util.*

class ServerDate(var inValue: String) {

    val dateFormat = SimpleDateFormat("yyyy-MM-ddThh:mm:ss")

    val rawValue: Long

    init {
        rawValue = (dateFormat.parse(inValue) as Date).time
    }

}
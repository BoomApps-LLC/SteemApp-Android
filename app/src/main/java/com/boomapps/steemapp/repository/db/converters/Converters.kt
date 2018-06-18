/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db.converters

import android.arch.persistence.room.TypeConverter

const val DELIMITER: String = "::##::"

class Converters {


    @TypeConverter
    fun stringArrayToString(input: Array<String>): String {
        return input.joinToString(DELIMITER)
    }


    @TypeConverter
    fun stringToStringArray(input: String): Array<String> {
        return input.split(DELIMITER.toRegex(), 0).toTypedArray()
    }

}
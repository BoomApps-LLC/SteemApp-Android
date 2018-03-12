package com.boomapps.steemapp

import android.content.res.Resources
import android.graphics.Color

/**
 * Created by Vitali Grechikha on 26.02.2018.
 */


fun Resources.getMatColor(typeColor: String): Int {
    var returnColor = Color.BLACK
    val arrayId = getIdentifier("mdcolor_" + typeColor, "array", SteemApplication.instance.applicationContext.getPackageName())

    if (arrayId != 0) {
        val colors = obtainTypedArray(arrayId)
        val index = (Math.random() * colors.length()).toInt()
        returnColor = colors.getColor(index, Color.BLACK)
        colors.recycle()
    }
    return returnColor

}
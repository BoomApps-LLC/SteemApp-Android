/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.InputFilter
import android.util.Log
import android.widget.EditText
import java.io.File
import java.io.IOException
import java.util.*
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


/**
 * Created by vgrechikha on 06.02.2018.
 */
class Utils {

    companion object {
        val instance = Utils()
        fun get(): Utils {
            return instance
        }
    }

    fun getNewTempUriForExternalApp(): Array<Uri?>? {
        val result = arrayOfNulls<Uri>(2)
        // Create an image file name
        //        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        val uniqueID = UUID.randomUUID().toString()
        val imageFileName = "JPEG_" + uniqueID + "_"
        var storageDir = SteemApplication.instance.externalCacheDir
        if (storageDir == null || !storageDir.isDirectory || !storageDir.canWrite()) {
            storageDir = SteemApplication.instance.cacheDir
            if (storageDir == null || !storageDir.isDirectory || !storageDir.canWrite()) {
                return null
            }
        }

        try {
            val image = File.createTempFile(
                    imageFileName, /* prefix */
                    ".jpg", /* suffix */
                    storageDir      /* directory */
            )
            result[0] = Uri.fromFile(image)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Log.d("Utils", "Authority: " + SteemApplication.instance.getPackageName() + ".GenericFileProvider")
                result[1] = FileProvider.getUriForFile(SteemApplication.instance, SteemApplication.instance.getPackageName() + ".GenericFileProvider", image)
            } else {
                result[1] = result[0]
            }
            return result
        } catch (e: IOException) {
            Log.d("A4A", "Cannot create temporary file for new photo")
            e.printStackTrace()
        }

        return null
    }

    fun getFileFromUri(uri: Uri): File {
        return File(uri.path)
    }


    fun getNewTempUri(): Uri? {
        // Create an image file name
        //        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        val uniqueID = UUID.randomUUID().toString()
        val imageFileName = "JPEG_" + uniqueID + "_"
        var storageDir = SteemApplication.instance.externalCacheDir
        if (storageDir == null || !storageDir.isDirectory || !storageDir.canWrite()) {
            storageDir = SteemApplication.instance.cacheDir
            if (storageDir == null || !storageDir.isDirectory || !storageDir.canWrite()) {
                return null
            }
        }

        try {
            val image = File.createTempFile(
                    imageFileName, /* prefix */
                    ".jpg", /* suffix */
                    storageDir      /* directory */
            )
            return Uri.fromFile(image)
        } catch (e: IOException) {
            Log.d("A4A", "Cannot create temporary file for new photo")
            e.printStackTrace()
        }

        return null
    }


    /**
     * Set max length of input value in EditText control
     *
     * @param et    ui EditText control
     * @param limit max length
     */
    fun setInputMaxLength(et: EditText?, limit: Int) {
        if (et != null) {
            et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(limit))
        }
    }

}
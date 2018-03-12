package com.boomapps.steemapp

import android.content.Context
import android.net.Uri
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable

/**
 * Created by Vitali Grechikha on 20.02.2018.
 */
class RxUtils {

    interface OnCopyFileResultListener {

        fun onResult(success: Boolean)

    }


    fun copyFile(sourceUri: Uri, destUri: Uri, context: Context, listener: OnCopyFileResultListener) {

        Observable
                .fromCallable({
                    try {
                        val pfdInput = SteemApplication.instance.getContentResolver().openFileDescriptor(sourceUri, "r")
                        val fdInput = pfdInput.getFileDescriptor()

                        val inputStream = FileInputStream(fdInput)

                        val pfdOutput = SteemApplication.instance.getContentResolver().openFileDescriptor(destUri, "w")
                        val fdOutput = pfdOutput.getFileDescriptor()

                        val out = FileOutputStream(fdOutput)
                        val buffer = ByteArray(1024)
                        var length: Int = inputStream.read(buffer)
                        while (length > 0) {
                            out.write(buffer, 0, length)
                            length = inputStream.read(buffer)
                        }
                        out.flush()
                        out.close()
                        inputStream.close()
                    } catch (ex: Exception) {
                        ex.fillInStackTrace()
                        return@fromCallable false
                    }
                    return@fromCallable true
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext({
                    listener.onResult(it)
                })
                .doOnError({
                    Log.d("RxUtils", "Copying file error")
                    listener.onResult(false)
                })
                .subscribe()

    }

}
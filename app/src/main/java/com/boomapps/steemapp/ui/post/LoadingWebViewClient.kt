/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.post

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.MalformedURLException
import java.net.URL

class LoadingWebViewClient(val listener: LoadingListener) : WebViewClient() {

    interface LoadingListener {
        fun showProgress()

        fun hideProgress()

        fun setTitle(title: String)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        try {
            view.loadUrl(URL(uri.toString()).toString())
        } catch (m: MalformedURLException) {
            m.printStackTrace()
        }


        return true
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        listener.setTitle(view.title)
        listener.showProgress()
        super.onPageStarted(view, url, favicon)
    }


    override fun onPageFinished(view: WebView, url: String) {
        listener.hideProgress()
        super.onPageFinished(view, url)
    }

}
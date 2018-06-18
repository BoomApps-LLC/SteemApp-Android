/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by Vitali Grechikha on 11.02.2018.
 */
class HeadersInterceptor(val headers: Map<String, String>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder();
        for ((header, value) in headers) {
            requestBuilder.addHeader(header, value)
        }
        return chain.proceed(requestBuilder.build())
    }
}
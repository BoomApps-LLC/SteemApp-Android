/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.network

import android.net.Uri
import com.boomapps.steemapp.repository.currency.AmountRequestData
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.repository.currency.TradingPairInfo
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.feed.FeedFullData
import io.reactivex.Observable
import java.net.URL

/**
 * Created by vgrechikha on 21.03.2018.
 */
interface NetworkRepository {

    interface OnRequestFinishCallback<R> {

        fun onSuccessRequestFinish(response: R)

        fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable)

    }

    var extendedProfileResponse: ProfileResponse?


    fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, callback: NetworkRepository.OnRequestFinishCallback<Any?>)

    fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, permlink: String?, callback: NetworkRepository.OnRequestFinishCallback<Any?>)

    fun uploadNewPhoto(uri: Uri, callback: NetworkRepository.OnRequestFinishCallback<URL?>)

    fun loadExtendedUserProfile(nick: String, callback: NetworkRepository.OnRequestFinishCallback<ProfileResponse?>)

    fun loadFullStartData(nick: String, callback: NetworkRepository.OnRequestFinishCallback<Any?>)

    fun loadExtendedPostData(name: String, permlink: String): Observable<FeedFullData>

    fun loadTradingPairs(callback: NetworkRepository.OnRequestFinishCallback<Array<TradingPairInfo>>)

    fun loadOutputAmount(requestData : AmountRequestData, callback: NetworkRepository.OnRequestFinishCallback<OutputAmount>)

    fun loadOutputAmounts(requestData : ArrayList<AmountRequestData>, callback: NetworkRepository
    .OnRequestFinishCallback<ArrayList<OutputAmount>>)

}
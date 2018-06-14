package com.boomapps.steemapp.repository.network

import android.net.Uri
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.feed.FeedFullData
import io.reactivex.Observable
import java.net.URL

/**
 * Created by vgrechikha on 21.03.2018.
 */
interface NetworkRepository {

    interface OnRequestFinishCallback {

        fun onSuccessRequestFinish()

        fun onFailureRequestFinish(throwable: Throwable)

    }


    var extendedProfileResponse: ProfileResponse?
    var coinmarketcapCurrency: CoinmarketcapCurrency?
    var lastUploadedPhotoUrl: URL?


    fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, callback: NetworkRepository.OnRequestFinishCallback)

    fun uploadNewPhoto(uri: Uri, callback: NetworkRepository.OnRequestFinishCallback)

    fun loadExtendedUserProfile(nick: String, callback: NetworkRepository.OnRequestFinishCallback)

    fun loadFullStartData(nick: String, callback: NetworkRepository.OnRequestFinishCallback)

    fun loadExtendedPostData(name : String, permlink : String) : Observable<FeedFullData>

}
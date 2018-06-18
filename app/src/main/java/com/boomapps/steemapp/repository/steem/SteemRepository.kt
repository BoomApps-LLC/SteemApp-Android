/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

import android.net.Uri
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion
import eu.bittrade.libs.steemj.apis.follow.model.CommentFeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.Permlink
import io.reactivex.Observable
import io.reactivex.Single
import java.net.URL

interface SteemRepository {


    interface Callback<T> {
        fun onSuccess(result: T)

        fun onError(error: Throwable)
    }


    fun isLogged(): Boolean

    /**
     * Fake login into SteemIt
     * Method only initialize {@link eu.bittrade.libs.steemj.configuration.SteemJConfig}
     * and {@link eu.bittrade.libs.steemj.SteemJ} objects
     */
    fun login(nickname: String, postingKey: String?): SteemWorkerResponse

    fun hasPostingKey() : Boolean

    fun signOut()

    fun post(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean): SteemWorker.PostingResult

    fun uploadImage(uri: Uri): URL?

    fun getVestingShares(): Array<Double>

    fun getStoryDetails(aName: AccountName?, pLink: Permlink, orderId: Int): Observable<DiscussionData>

    fun getFeedShortDataList(aName: AccountName?): Observable<ArrayList<StoryShortEntry>>

    fun getFeedShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>>

    fun getBlogShortDataList(aName: AccountName?): Observable<ArrayList<StoryShortEntry>>


    fun getBlogShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>>


    fun getFeedStories(aName: AccountName?, start: Int, limit: Int): Single<ArrayList<DiscussionData>>


    fun getBlogStories(aName: AccountName?, start: Int, limit: Int): Single<ArrayList<DiscussionData>>

    fun getTrendingDataList(start: Int, limit: Int, storyEntity: StoryEntity?): Single<ArrayList<DiscussionData>>?

    fun getNewDataList(start: Int, limit: Int, storyEntity: StoryEntity?): Single<ArrayList<DiscussionData>>?

    fun vote(postPermLink: String, percentage: Int)

    fun vote(authorFor: String, postPermLink: String, percentage: Int) : Boolean

    fun cancelVote(postPermLink: String)

    fun cancelVote(author: String, postPermLink: String) : Boolean

    fun unvoteWithUpdate(story: StoryEntity, type: FeedType, callback: Callback<Boolean>)

    fun voteWithUpdate(story: StoryEntity, type: FeedType, percent: Int, callback: Callback<Boolean>)

    fun updatePostingKey(stringExtra: String?)
}
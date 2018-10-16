/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.annotation.MainThread
import com.boomapps.steemapp.repository.db.DiscussionMapper
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.steem.DiscussionData
import com.boomapps.steemapp.repository.steem.SteemRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.Executor

class FeedBoundaryCallback(
        private val type: FeedType,
        private val steemRepo: SteemRepository,
        private val handleResponse: (FeedType, Array<StoryEntity>) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int) : PagedList.BoundaryCallback<StoryEntity>() {


    val networkState = MutableLiveData<NetworkState>()


    private fun insertItemsIntoDb(items: ArrayList<DiscussionData>) {
        ioExecutor.execute {
            handleResponse(type, DiscussionMapper(items, RepositoryProvider.getPreferencesRepository().loadUserData().nickname
                    ?: "_").map().toTypedArray())
        }
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        networkState.value = NetworkState.LOADING
        Timber.d("onZeroItemsLoaded($type)")
        val observable = when (type) {
            FeedType.BLOG -> RepositoryProvider.getSteemRepository().getBlogStories(null, 0, networkPageSize)
            FeedType.TRENDING -> RepositoryProvider.getSteemRepository().getTrendingDataList(0, networkPageSize, null)
            FeedType.NEW -> RepositoryProvider.getSteemRepository().getNewDataList(0, networkPageSize, null)
            else -> /*FeedType.FEED*/ RepositoryProvider.getSteemRepository().getFeedStories(null, 0, networkPageSize)
        }

        observable?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    insertItemsIntoDb(it)
                    networkState.postValue(NetworkState.LOADED)
                }, {
                    Timber.e(it)
                    networkState.postValue(NetworkState.error("Loading data error for " + type.name))
                })


    }

    fun retryAllFailed() {
        // TODO implement retying loading post which were loaded with error
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: StoryEntity) {
        networkState.value = NetworkState.LOADING
        Timber.d(" >> onItemAtEndLoaded($type)")
        val single = when (type) {
            FeedType.BLOG -> RepositoryProvider.getSteemRepository().getBlogStories(null, itemAtEnd.indexInResponse, networkPageSize)
            FeedType.TRENDING -> RepositoryProvider.getSteemRepository().getTrendingDataList(itemAtEnd.indexInResponse, networkPageSize, itemAtEnd)
            FeedType.NEW -> RepositoryProvider.getSteemRepository().getNewDataList(itemAtEnd.indexInResponse, networkPageSize, itemAtEnd)
            else -> /*FeedType.FEED*/ RepositoryProvider.getSteemRepository().getFeedStories(null, itemAtEnd.indexInResponse, networkPageSize)
        }
        single?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    insertItemsIntoDb(it)
                    networkState.postValue(NetworkState.LOADED)
                }, {
                    Timber.e(it)
                    networkState.postValue(NetworkState.error("Loading data error for " + type.name))
                })
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: StoryEntity) {
        super.onItemAtFrontLoaded(itemAtFront)
        Timber.d("onItemAtFrontLoaded(itemAtFront=[%d, %s])", itemAtFront.indexInResponse, itemAtFront.permlink)
    }
}
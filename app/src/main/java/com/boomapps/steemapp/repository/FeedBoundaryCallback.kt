package com.boomapps.steemapp.repository

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList
import android.os.Handler
import android.support.annotation.MainThread
import com.boomapps.steemapp.repository.db.DiscussionToStoryMapper
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
            handleResponse(type, DiscussionToStoryMapper(items).map().toTypedArray())
        }
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        networkState.value = NetworkState.LOADING
        Timber.d("onZeroItemsLoaded()")
        val observable = when (type) {
            FeedType.BLOG -> ServiceLocator.getSteemRepository().getBlogStories(null, 0, networkPageSize)
            FeedType.TRENDING -> ServiceLocator.getSteemRepository().getTrendingDataList(0, networkPageSize, "")
            FeedType.NEW -> ServiceLocator.getSteemRepository().getNewDataList(0, networkPageSize, "")
            else -> /*FeedType.FEED*/ ServiceLocator.getSteemRepository().getFeedStories(null, 0, networkPageSize)
        }

        observable?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    insertItemsIntoDb(it)
                    networkState.value = NetworkState.LOADED
                }, {
                    Timber.e(it)
                    networkState.value = NetworkState.error("Loading data error for " + type.name)
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
        Timber.d("onItemAtEndLoaded(itemAtEnd=[%d, %s])", itemAtEnd.indexInResponse, itemAtEnd.permlink)
        val single = when (type) {
            FeedType.BLOG -> ServiceLocator.getSteemRepository().getBlogStories(null, itemAtEnd.indexInResponse, networkPageSize)
            FeedType.TRENDING -> ServiceLocator.getSteemRepository().getTrendingDataList(itemAtEnd.indexInResponse, networkPageSize, itemAtEnd.permlink)
            FeedType.NEW -> ServiceLocator.getSteemRepository().getNewDataList(itemAtEnd.indexInResponse, networkPageSize, itemAtEnd.permlink)
            else -> /*FeedType.FEED*/ ServiceLocator.getSteemRepository().getFeedStories(null, itemAtEnd.indexInResponse, networkPageSize)
        }
        single?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    insertItemsIntoDb(it)
                    Handler().postDelayed({
                        networkState.value = NetworkState.LOADED
                    }, 2000)
                }, {
                    Timber.e(it)
                    networkState.value = NetworkState.error("Loading data error for " + type.name)
                })
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: StoryEntity) {
        super.onItemAtFrontLoaded(itemAtFront)
        Timber.d("onItemAtFrontLoaded(itemAtFront=[%d, %s])", itemAtFront.indexInResponse, itemAtFront.permlink)
    }
}
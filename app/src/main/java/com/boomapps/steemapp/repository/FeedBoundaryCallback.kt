package com.boomapps.steemapp.repository

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import com.boomapps.steemapp.repository.db.DiscussionToStoryMapper
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.steem.DiscussionData
import com.boomapps.steemapp.repository.steem.SteemRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Executor

class FeedBoundaryCallback(
        private val type: FeedType,
        private val steemRepo: SteemRepository,
        private val handleResponse: (FeedType, Array<StoryEntity>) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int) : PagedList.BoundaryCallback<StoryEntity>() {


    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()


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
        Timber.d("onZeroItemsLoaded()")
        val single: Single<ArrayList<DiscussionData>> = if (type == FeedType.FEED) {
            // FEED
            ServiceLocator.getSteemRepository().getFeedStories(null, 0, networkPageSize)
        } else {
            // BLOG
            ServiceLocator.getSteemRepository().getBlogStories(null, 0, networkPageSize)
        }
        single.observeOn(Schedulers.io())
                .subscribe({
                    insertItemsIntoDb(it)
                }, {
                    Timber.e(it)
                })


    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: StoryEntity) {
        Timber.d("onItemAtEndLoaded(itemAtEnd=[%d, %s])", itemAtEnd.indexInResponse, itemAtEnd.permlink)
        val single: Single<ArrayList<DiscussionData>> = if (type == FeedType.FEED) {
            // FEED
            ServiceLocator.getSteemRepository().getFeedStories(null, itemAtEnd.indexInResponse, networkPageSize)
        } else {
            // BLOG
            ServiceLocator.getSteemRepository().getBlogStories(null, itemAtEnd.indexInResponse, networkPageSize)
        }
        single.observeOn(Schedulers.io())
                .subscribe({
                    insertItemsIntoDb(it)
                }, {
                    Timber.e(it)
                })
    }

    @MainThread
    override fun onItemAtFrontLoaded(itemAtFront: StoryEntity) {
        super.onItemAtFrontLoaded(itemAtFront)
        // ignored, since we only ever append to what's in the DB
    }
}
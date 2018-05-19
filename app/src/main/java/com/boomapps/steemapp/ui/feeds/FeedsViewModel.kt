package com.boomapps.steemapp.ui.feeds

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.PagedList
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.NetworkState
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.ui.BaseViewModel


class FeedsViewModel : BaseViewModel() {

    // blog
    private val blogType = MutableLiveData<FeedType>()
    private val blogRepoResult = Transformations.map(blogType, {
        ServiceLocator.getDaoRepository().storiesFor(it, ServiceLocator.DATABASE_PAGE_SIZE)
    })
    val blogs = Transformations.switchMap(blogRepoResult, { it.pagedList })!!
    val blogsNetworkState = Transformations.switchMap(blogRepoResult, { it.networkState })!!
    val blogsRefreshState = Transformations.switchMap(blogRepoResult, { it.refreshState })!!

    private val feedType = MutableLiveData<FeedType>()
    private val feedRepoResult = Transformations.map(feedType, {
        ServiceLocator.getDaoRepository().storiesFor(it, ServiceLocator.DATABASE_PAGE_SIZE)
    })

    val feeds = Transformations.switchMap(feedRepoResult, { it.pagedList })!!
    val feedsNetworkState = Transformations.switchMap(feedRepoResult, { it.networkState })!!
    val feedsRefreshState = Transformations.switchMap(feedRepoResult, { it.refreshState })!!

    fun getListLiveData(type: FeedType): LiveData<PagedList<StoryEntity>> {

        if (type == FeedType.FEED) {
            return feeds
        }
        // default is blogs
        return blogs
    }

    fun getNetworkState(type: FeedType): LiveData<NetworkState> {
        if (type == FeedType.FEED) {
            return feedsNetworkState
        }
        // default is blogs
        return blogsNetworkState
    }

    fun getRefreshState(type: FeedType): LiveData<NetworkState> {
        if (type == FeedType.FEED) {
            return feedsRefreshState
        }

        return blogsRefreshState
    }

    fun refresh(type: FeedType) {
        when (type) {
            FeedType.BLOG -> blogRepoResult.value?.refresh?.invoke()
            FeedType.FEED -> feedRepoResult.value?.refresh?.invoke()
        }
    }

    fun showList(type: FeedType): Boolean {
        when (type) {
            FeedType.BLOG -> {
                if (blogType.value != type) {
                    blogType.value = type
                } else {
                    return false
                }
            }
            FeedType.FEED -> {
                if (feedType.value != type) {
                    feedType.value = type
                } else {
                    return false
                }
            }
        }
        return true
    }


    fun retry(type: FeedType) {
        val listing = when (type) {
            FeedType.BLOG -> blogRepoResult?.value
            FeedType.FEED -> feedRepoResult?.value
            else -> blogRepoResult?.value // default value
        }
        listing?.retry?.invoke()
    }

    fun isActivated(type: FeedType): Boolean {
        if (type == FeedType.FEED) {
            return feedType.value == type
        }

        // default blog
        return blogType.value == FeedType.BLOG
    }

}
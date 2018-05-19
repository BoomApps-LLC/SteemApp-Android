package com.boomapps.steemapp.repository.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.boomapps.steemapp.repository.*
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.steem.DiscussionData
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.Permlink
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Executor

class DaoRepositoryDefault(
        val db: AppDatabase,
        private val ioExecutor: Executor,
        private val networkPageSize: Int = ServiceLocator.NETWORK_PAGE_SIZE) : DaoRepository {


    override fun insertBlogEntities(blogEntities: ArrayList<StoryEntity>) {
        db.storiesDao().insertStories(blogEntities.toTypedArray())
    }


    override fun insertFeedEntities(blogEntities: ArrayList<StoryEntity>) {
        db.storiesDao().insertStories(blogEntities.toTypedArray())
    }

//    override fun getFeedShortEntities(): LiveData<Array<StoryShortEntity>> {
//        return db.storiesDao().shortStoriesByType(FeedType.FEED.ordinal)
//    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    fun insertResultIntoDb(type: FeedType, data: Array<StoryEntity>) {
        db.runInTransaction {
            //            val start = db.storiesDao().getNextIndexInStories(type.ordinal)
            val items = data.mapIndexed { index, storyEntity ->
                //                storyEntity.indexInResponse = start + index
                storyEntity.storyType = type.ordinal
                storyEntity
            }.toTypedArray()
            db.storiesDao().insertStories(items)
        }
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */

    @MainThread
    override fun refresh(type: FeedType): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        val discussions: ArrayList<DiscussionData> = arrayListOf()
        val obs = if (type == FeedType.BLOG) {
            ServiceLocator.getSteemRepository().getBlogShortDataList(null, 0, ServiceLocator.NETWORK_PAGE_SIZE)
        } else {
            ServiceLocator.getSteemRepository().getFeedShortDataList(null, 0, ServiceLocator.NETWORK_PAGE_SIZE)
        }
        obs.subscribeOn(Schedulers.io())
                .take(networkPageSize.toLong())
                .flatMap {
                    Timber.d("Feed: call flat map")
                    return@flatMap Observable.fromIterable(it)
                            .doOnNext {
                                Timber.d("Feed: First fromIterable >> permlink = ${it.permlink}")
                            }
                            .flatMap {
                                return@flatMap ServiceLocator.getSteemRepository().getStoryDetails(AccountName(it.author), Permlink(it.permlink), it.id)
                            }
                            .doOnNext{
                                Timber.d("Feed: onNext Details for(%s) >> title = %s", it.discussion?.permlink, it.discussion?.title)
                            }
                            .subscribeOn(Schedulers.io())
                }
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it != null && it.discussion != null) {
                                Timber.d("Feed: OnNext(${it.discussion.title})")
                                discussions.add(it)
                            } else {
                                Timber.w("Feed: OnNext result is %s", "null")
                            }
                        },
                        {
                            networkState.value = NetworkState.error(it.message)
                        },
                        {

                            // process new discussions
                            val result = discussions.map {
                                return@map DiscussionToStoryMapper((it)).map()[0]
                            }.toTypedArray()
                            db.runInTransaction {
                                // clear data for type
                                db.storiesDao().deleteStoriesFor(type.ordinal)
                                // save new data intoDB
                                insertResultIntoDb(type, result)
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)

                        })
        return networkState
    }


    /**
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    override fun storiesFor(type: FeedType, pageSize: Int): Listing<StoryEntity> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = FeedBoundaryCallback(
                type,
                ServiceLocator.getSteemRepository(),
                handleResponse = this::insertResultIntoDb,
                ioExecutor = ioExecutor,
                networkPageSize = networkPageSize)
        // create a data source factory from Room
        val dataSourceFactory = db.storiesDao().loadAllStories(type.ordinal)
        val builder = LivePagedListBuilder(dataSourceFactory, pageSize)
                .setBoundaryCallback(boundaryCallback)

        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger, {
            refresh(type)
        })

        return Listing(
                pagedList = builder.build(),
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }

}
package com.boomapps.steemapp.ui.feeds

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.text.Html
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.db.entities.BaseStoryEntity
import com.boomapps.steemapp.repository.db.entities.BlogEntity
import com.boomapps.steemapp.repository.db.entities.FeedEntity
import com.boomapps.steemapp.repository.feed.FeedMetadata
import com.boomapps.steemapp.ui.BaseViewModel
import com.google.gson.GsonBuilder
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion
import eu.bittrade.libs.steemj.base.models.Permlink
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class FeedsViewModel : BaseViewModel() {

    var feedType = FeedType.BLOG

    var blogData: MediatorLiveData<ArrayList<FeedCardViewData>> = MediatorLiveData()

    var feedData: MediatorLiveData<ArrayList<FeedCardViewData>> = MediatorLiveData()

    var trendingData: MediatorLiveData<ArrayList<FeedCardViewData>> = MediatorLiveData()

    var newData: MediatorLiveData<ArrayList<FeedCardViewData>> = MediatorLiveData()

    init {
        initBlogDataSource()
        feedData.value = null
    }

    fun refresh(type: FeedType){

    }

    fun retry(type: FeedType){

    }

    fun initBlogDataSource() {
        blogData.value = null
        val lData = RepositoryProvider.instance.getDaoRepository().loadAllBlogEntities()
        blogData.addSource(lData, object : Observer<Array<BlogEntity>> {
            override fun onChanged(t: Array<BlogEntity>?) {
                if (t != null) {
                    val newData = ArrayList(t.map { FeedCardViewData(it) })
                    val curData = blogData.value
                    Timber.d("BLOG onChanged(${t.size})")
                    if (curData != null) {
                        curData.addAll(newData)
                        blogData.value = curData
                    } else {
                        blogData.value = newData
                    }
                }
            }
        })
    }


    fun getData(type: FeedType): MutableLiveData<ArrayList<FeedCardViewData>> {
        return when (type) {
            FeedType.BLOG -> getDataForBlog()
            FeedType.FEED -> getDataForFeed()
            FeedType.NEW -> getDataForNew()
            FeedType.TRENDING -> getDataForTrends()
        }
    }

    fun getDataForBlog(): MutableLiveData<ArrayList<FeedCardViewData>> {
        // TODO correct network request
        if (blogData.value == null) {
            loadDisscussionsFor(FeedType.BLOG)
        }
        return blogData
    }

    fun getDataForFeed(): MutableLiveData<ArrayList<FeedCardViewData>> {
        if (feedData.value == null) {
            feedData.addSource(
                    RepositoryProvider.instance.getDaoRepository().loadAllFeedEntities(),
                    object : Observer<Array<FeedEntity>> {
                        override fun onChanged(t: Array<FeedEntity>?) {
                            if (t != null) {
                                feedData.value = ArrayList(t.map { FeedCardViewData(it) })
                            }

                        }
                    })

            loadDisscussionsFor(FeedType.FEED)
        }
        return feedData
    }

    fun getDataForTrends(): MutableLiveData<ArrayList<FeedCardViewData>> {
        if (trendingData.value == null) {
            trendingData.addSource(
                    RepositoryProvider.instance.getDaoRepository().loadAllBlogEntities(),
                    object : Observer<Array<BlogEntity>> {
                        override fun onChanged(t: Array<BlogEntity>?) {
                            if (t != null) {
                                trendingData.value = ArrayList(t.map { FeedCardViewData(it) })
                            }

                        }
                    })

            loadDisscussionsFor(FeedType.BLOG)
        }
        return trendingData
    }

    fun getDataForNew(): MutableLiveData<ArrayList<FeedCardViewData>> {
        if (newData.value == null) {
            newData.addSource(
                    RepositoryProvider.instance.getDaoRepository().loadAllBlogEntities(),
                    object : Observer<Array<BlogEntity>> {
                        override fun onChanged(t: Array<BlogEntity>?) {
                            if (t != null) {
                                newData.value = ArrayList(t.map { FeedCardViewData(it) })
                            }

                        }
                    })

            loadDisscussionsFor(FeedType.BLOG)
        }
        return newData
    }


    val pattern = Regex("(\\s+)|(\\\\n+)")

    private fun <R : BaseStoryEntity> convertToStory(inValue: Discussion, outValue: R) {
        outValue.entityId = inValue.id
        outValue.title = inValue.title
        outValue.author = inValue.author.name
        outValue.rawBody = inValue.body
        if (inValue.body.startsWith("![apt.JPG]")) {
            outValue.shortText = Html.fromHtml(inValue.body.substringAfter(")")).toString()
        } else {
            outValue.shortText = inValue.body
        }
        val builder = StringBuilder()
        val lines = outValue.shortText.split(pattern)
        if (lines.size > 1) {
            for (line in lines) {
                if (!line.startsWith("http")) {
                    builder.append(line).append(" ")
                }
            }
            val formatted = Html.fromHtml(builder.toString()).trim()
            outValue.shortText = formatted.substring(0, Math.min(formatted.length, 180))
        }

        if (inValue.jsonMetadata != null) {
            val metadata = parseMetadata(inValue.jsonMetadata)
            if (metadata.imagesUrl.isNotEmpty()) {
                outValue.images = metadata.imagesUrl
                outValue.mainImageUrl = metadata.imagesUrl[0]
            }
            outValue.tags = metadata.tags
            outValue.links = metadata.links
        }
        outValue.linksNum = outValue.links.size
        outValue.votesNum = inValue.netVotes
    }


    private fun parseMetadata(input: String): FeedMetadata {
        if (input.isEmpty()) {
            return FeedMetadata()
        }
        val gson = GsonBuilder().create()
        return gson.fromJson<FeedMetadata>(input, FeedMetadata::class.java)
    }


    fun loadDisscussionsFor(type: FeedType) {

        when (type) {
            FeedType.BLOG -> loadBlogData()
            FeedType.FEED -> loadFeedData()
        }

    }

    private fun loadFeedData() {
        val allData: ArrayList<Discussion> = arrayListOf()
        val obs = RepositoryProvider.instance.getSteemRepository().getFeedShortDataList(null)
        obs
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    Timber.d("Feed: First request >> size = ${it.size}")
                }
                .flatMap {
                    Timber.d("Feed: call flat map")
                    return@flatMap Observable.fromIterable(it)
                            .doOnNext {
                                Timber.d("Feed: First fromIterable >> permlink = ${it.permlink}")
                            }
                            .flatMap {
                                return@flatMap RepositoryProvider.instance.getSteemRepository().getPostDetails(null, Permlink(it.permlink))
                            }
                            .subscribeOn(Schedulers.io())

                }
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it != null && it.body.isNotEmpty()) {
                                Timber.d("Feed: OnNext(${it.title})")
                                allData.add(it)
                            } else {
                                Timber.w("Feed: OnNext result is %s", "null")
                            }

                        },
                        {
                            Timber.e(it)
                        },
                        {
                            val result = ArrayList(allData.map {
                                val outValue = FeedEntity()
                                convertToStory(it, outValue)
                                return@map outValue
                            })
                            RepositoryProvider.instance.getDaoRepository().insertFeedEntities(result)
                        })
    }


    private fun loadBlogData() {
        val allData: ArrayList<Discussion> = arrayListOf()
        val obs = RepositoryProvider.instance.getSteemRepository().getBlogShortDataList(null)
        obs
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    Timber.d("Blog: First request >> size = ${it.size}")
                }
                .flatMap {
                    Timber.d("Blog: call flat map")
                    return@flatMap Observable.fromIterable(it)
                            .doOnNext {
                                Timber.d("Blog: First fromIterable >> permlink = ${it.permlink}")
                            }
                            .flatMap {
                                return@flatMap RepositoryProvider.instance.getSteemRepository().getPostDetails(null, Permlink(it.permlink))
                            }
                            .subscribeOn(Schedulers.io())

                }
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it != null && it.body.isNotEmpty()) {
                                Timber.d("Blog: OnNext(${it.title})")
                                allData.add(it)
                            } else {
                                Timber.w("Blog: OnNext result is %s", "null")
                            }

                        },
                        {
                            Timber.e(it)
                        },
                        {
                            val result: ArrayList<BlogEntity> = ArrayList(allData.map {
                                val outValue = BlogEntity()
                                convertToStory(it, outValue)
                                return@map outValue
                            })
                            RepositoryProvider.instance.getDaoRepository().insertBlogEntities(result)
                        })
    }

}
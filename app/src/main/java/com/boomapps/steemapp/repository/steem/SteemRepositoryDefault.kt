/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

import android.net.Uri
import com.boomapps.steemapp.BuildConfig
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.db.DiscussionToStoryMapper
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import eu.bittrade.crypto.core.AddressFormatException
import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.DiscussionQuery
import eu.bittrade.libs.steemj.base.models.Permlink
import eu.bittrade.libs.steemj.configuration.SteemJConfig
import eu.bittrade.libs.steemj.enums.DiscussionSortType
import eu.bittrade.libs.steemj.enums.PrivateKeyType
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemConnectionException
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import eu.bittrade.libs.steemj.image.upload.SteemJImageUpload
import eu.bittrade.libs.steemj.image.upload.config.SteemJImageUploadConfig
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.tuple.ImmutablePair
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.URL
import java.security.InvalidParameterException

class SteemRepositoryDefault : SteemRepository {

    private var steemJ: SteemJ? = null
    private var steemJConfig: SteemJConfig? = null


    override fun isLogged(): Boolean {
        return steemJ != null && steemJConfig != null
    }


    override fun signOut() {
        steemJConfig = null
        steemJ = null
    }

    override fun hasPostingKey(): Boolean {
        if (steemJ == null) {
            return false
        }
        if (steemJConfig?.privateKeyStorage != null) {
            val accountName = steemJConfig?.defaultAccount
            if (accountName != null) {
                val accounts = steemJConfig?.privateKeyStorage?.accounts
                if (accounts == null || !accounts.contains(accountName)) {
                    return false
                }
                val allKeysForAccount = steemJConfig?.privateKeyStorage?.privateKeysPerAccounts?.get(accountName)
                        ?: return false
                for (imPair in allKeysForAccount) {
                    if (imPair.left == PrivateKeyType.POSTING) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun updatePostingKey(postingKey: String?) {
        val privateKeys = arrayListOf<ImmutablePair<PrivateKeyType, String>>()
        if (postingKey != null && postingKey.isNotEmpty()) {
            privateKeys.add(ImmutablePair(PrivateKeyType.POSTING, postingKey))
        }
        steemJConfig?.privateKeyStorage?.addAccount(steemJConfig?.defaultAccount, privateKeys);
        steemJ = SteemJ()
    }

    /**
     *
     */
    override fun login(nickname: String, postingKey: String?): SteemWorkerResponse {
        try {
            steemJConfig = SteemJConfig.getInstance()
            steemJConfig?.setAppName("SteemApp")
            steemJConfig?.setAppVersion(BuildConfig.VERSION_NAME)

            steemJConfig?.defaultAccount = AccountName(nickname)
            steemJConfig?.responseTimeout = 10000
            val privateKeys = arrayListOf<ImmutablePair<PrivateKeyType, String>>()
            if (postingKey != null && postingKey.isNotEmpty()) {
                privateKeys.add(ImmutablePair(PrivateKeyType.POSTING, postingKey))
            }
            if (privateKeys.size > 0) {
                steemJConfig?.privateKeyStorage?.addAccount(steemJConfig?.defaultAccount, privateKeys);
            } else {
                steemJConfig?.privateKeyStorage?.addAccount(steemJConfig?.defaultAccount);
            }

            steemJ = SteemJ()
            var result = steemJ?.lookupAccounts(nickname, 1)
            if (result == null || result.size == 0 || result[0].toLowerCase() != nickname.toLowerCase()) {
                return SteemWorkerResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, null)
            }
        } catch (sce: SteemConnectionException) {
            Timber.e(sce)
            sce.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.CONNECTION_ERROR, null)
        } catch (sre: SteemResponseException) {
            Timber.e(sre)
            sre.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.TIMEOUT_ERROR, null)
        } catch (afe: AddressFormatException) {
            Timber.e(afe)
            afe.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, null)
        } catch (ipe: InvalidParameterException) {
            Timber.e(ipe)
            ipe.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, ipe.message)
        } catch (ex: Exception) {
            Timber.e(ex)
            ex.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.UNDEFINED_ERROR, null)
        }
        return SteemWorkerResponse(true, SteemErrorCodes.EMPTY_ERROR, null)
    }

    private fun addNewPostingKey(key: String) {
        steemJConfig?.privateKeyStorage?.addPrivateKeyToAccount(steemJConfig?.defaultAccount, ImmutablePair(PrivateKeyType.POSTING, key))
    }

    override fun post(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean): SteemWorker.PostingResult {
        try {
//            steemJConfig?.privateKeyStorage
            if (steemJConfig?.privateKeyStorage?.privateKeysPerAccounts == null || steemJConfig?.privateKeyStorage?.privateKeysPerAccounts?.size == 0) {

                var withKey = false
                if (postingKey.isNotEmpty()) {
                    steemJConfig?.privateKeyStorage?.addPrivateKeyToAccount(steemJConfig?.defaultAccount, ImmutablePair(PrivateKeyType.POSTING, postingKey))
                    withKey = true
                }

                if (!withKey) {
                    val uData = RepositoryProvider.getPreferencesRepository().loadUserData()
                    if (uData.postKey != null) {
                        addNewPostingKey(uData.postKey)
                        withKey = true
                    }
                }
                if (withKey) {
                    return postWithKey(title, content, tags, rewardsPercent, upvote)
                } else {
                    return SteemWorker.PostingResult("Posting key is empty.", false)
                }

            } else {
                return postWithKey(title, content, tags, rewardsPercent, upvote)
            }
        } catch (communicationException: SteemCommunicationException) {
            Timber.e(communicationException)
            return SteemWorker.PostingResult(communicationException.localizedMessage, false)
        } catch (responseException: SteemResponseException) {
            Timber.e(responseException)
            return SteemWorker.PostingResult(responseException.localizedMessage, false)
        } catch (transactionException: SteemInvalidTransactionException) {
            Timber.e(transactionException)
            return SteemWorker.PostingResult(transactionException.localizedMessage, false)
        } catch (parameterException: InvalidParameterException) {
            Timber.e(parameterException)
            return SteemWorker.PostingResult(parameterException.localizedMessage, false)
        }
        return SteemWorker.PostingResult()
    }

    private fun postWithKey(title: String, content: String, tags: Array<String>, rewardsPercent: Short, upvote: Boolean): SteemWorker.PostingResult {
        val commentOperation = steemJ?.createPostSynchronous(title, content, tags, rewardsPercent)
        if (!upvote) {
            return SteemWorker.PostingResult()
        }
        if (commentOperation != null) {
            val permlink = commentOperation.permlink.link
            steemJ?.vote(steemJConfig?.defaultAccount, Permlink(permlink), 100.toShort())
            return SteemWorker.PostingResult()
        } else {
            return SteemWorker.PostingResult("No comment operation.", false)
        }
    }

    override fun uploadImage(uri: Uri): URL {
        val imageFile = File(uri.path)
        if (!imageFile.exists() || imageFile.isDirectory || !imageFile.canRead()) {
            Timber.e("File or directory of image doesn't exist or cannot be read: " + uri.path)
            return URL("http://empty.com")
        }
        val config = SteemJImageUploadConfig.getInstance()
        config.connectTimeout = 30000
        config.readTimeout = 30000
        val uData = RepositoryProvider.getPreferencesRepository().loadUserData()
        var url = URL("http://empty.com")
        try {
            url = SteemJImageUpload.uploadImage(
                    eu.bittrade.libs.steemj.image.upload.models.AccountName(uData.nickname),
                    uData.postKey,
                    imageFile)
        } catch (e: IOException) {
            Timber.e(e, "Image upload exception")
        } finally {
            return url
        }
    }

    override fun getVestingShares(): Array<Double> {
        val dgProperties = steemJ?.dynamicGlobalProperties
        val tFund = dgProperties?.totalVestingFundSteem?.toReal()
        val tShares = dgProperties?.totalVestingShares?.toReal()
        if (tFund == null || tShares == null) {
            return arrayOf()
        } else {
            return arrayOf(tFund, tShares)
        }
    }

    override fun getFeedShortDataList(aName: AccountName?): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.FEED, steemJ, aName).load() }
    }


    override fun getFeedShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.FEED, steemJ, aName, start, limit).load() }
    }

    override fun getBlogShortDataList(aName: AccountName?): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.BLOG, steemJ, aName).load() }
    }

    override fun getBlogShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.BLOG, steemJ, aName, start, limit).load() }
    }

    override fun getStoryDetails(aName: AccountName?, pLink: Permlink, orderId: Int): Observable<DiscussionData> {
        val rqName = aName ?: steemJConfig?.defaultAccount
        Timber.d("getStoryDetails(aName=${rqName?.name}; permlink=${pLink.link}; orderId=$orderId)")
        return Observable.fromCallable { DiscussionData(orderId, steemJ?.getContent(rqName, pLink)) }
    }


    override fun getFeedStories(aName: AccountName?, start: Int, limit: Int): Single<ArrayList<DiscussionData>> {
        return getFeedShortDataList(aName, start, limit)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    return@flatMap Observable
                            .fromIterable(it)
                            .flatMap {
                                return@flatMap getStoryDetails(AccountName(it.author), Permlink(it.permlink), it.id)
                            }
                }
                .toList()
                .map {
                    val arrayList: ArrayList<DiscussionData> = arrayListOf()
                    for (discussionData in it) {
                        if (discussionData.discussion != null) {
                            arrayList.add(discussionData)
                        }
                    }
                    return@map arrayList
                }

    }

    override fun getBlogStories(aName: AccountName?, start: Int, limit: Int): Single<ArrayList<DiscussionData>> {
        return getBlogShortDataList(aName, start, limit)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    return@flatMap Observable
                            .fromIterable(it)
                            .flatMap {
                                return@flatMap getStoryDetails(AccountName(it.author), Permlink(it.permlink), it.id)
                            }
                }
                .toList()
                .map {
                    val arrayList: ArrayList<DiscussionData> = arrayListOf()
                    for (discussionData in it) {
                        if (discussionData.discussion != null) {
                            arrayList.add(discussionData)
                        }
                    }
                    return@map arrayList
                }

    }


    private fun getDiscussionsDataList(sortType: DiscussionSortType, start: Int, limit: Int, storyEntity: StoryEntity?): Single<ArrayList<DiscussionData>>? {
        val dQuery = DiscussionQuery()
        dQuery.limit = limit
        if (start > 0 && storyEntity != null) {
            dQuery.startPermlink = Permlink(storyEntity.permlink)
            dQuery.startAuthor = AccountName(storyEntity.author)
        }
        val observable = Single.fromCallable {
            return@fromCallable steemJ?.getDiscussionsBy(dQuery, sortType)
        }
                .subscribeOn(Schedulers.io())
                .map {
                    val arrayList: ArrayList<DiscussionData> = arrayListOf()
                    for ((index, discussion) in it.withIndex()) {
                        arrayList.add(DiscussionData(start + index, discussion))
                    }
                    return@map arrayList
                }
        return observable
    }

    override fun getTrendingDataList(start: Int, limit: Int, storyEntity: StoryEntity?): Single<ArrayList<DiscussionData>>? {
        return getDiscussionsDataList(DiscussionSortType.GET_DISCUSSIONS_BY_TRENDING, start, limit, storyEntity)
    }

    override fun getNewDataList(start: Int, limit: Int, storyEntity: StoryEntity?): Single<ArrayList<DiscussionData>>? {
        return getDiscussionsDataList(DiscussionSortType.GET_DISCUSSIONS_BY_CREATED, start, limit, storyEntity)
    }


    /*
        * Upvote the post
        * "steem-java-api-learned-to-speak-graphene-update-5" written by
        * "dez1337" using 100% of the defaultAccounts voting power.
        */
    override fun vote(postPermLink: String, percentage: Int) {
        try {
            steemJ?.vote(steemJConfig?.apiUsername, Permlink(postPermLink),
                    percentage.toShort())
        } catch (communicationException: SteemCommunicationException) {

        } catch (responseException: SteemResponseException) {

        } catch (transactionException: SteemInvalidTransactionException) {

        }
    }


    /*
        * Upvote the post
        * "steem-java-api-learned-to-speak-graphene-update-5" written by
        * "dez1337" using 100% of the defaultAccounts voting power.
        */
    override fun vote(author: String, postPermLink: String, percentage: Int): Boolean {
        try {
            steemJ?.vote(AccountName(author), Permlink(postPermLink),
                    percentage.toShort())
            return true
        } catch (communicationException: SteemCommunicationException) {
            return false
        } catch (responseException: SteemResponseException) {
            return false
        } catch (transactionException: SteemInvalidTransactionException) {
            return false
        }
        return false
    }

    override fun cancelVote(postPermLink: String) {
        try {
            steemJ?.cancelVote(steemJConfig?.apiUsername,
                    Permlink(postPermLink))
        } catch (communicationException: SteemCommunicationException) {

        } catch (responseException: SteemResponseException) {

        } catch (transactionException: SteemInvalidTransactionException) {

        }
    }

    override fun cancelVote(author: String, postPermLink: String): Boolean {
        try {
            steemJ?.cancelVote(AccountName(author),
                    Permlink(postPermLink))
            return true
        } catch (communicationException: SteemCommunicationException) {
            return false
        } catch (responseException: SteemResponseException) {
            return false
        } catch (transactionException: SteemInvalidTransactionException) {
            return false
        }
        return false
    }

    override fun unvoteWithUpdate(story: StoryEntity, type: FeedType, callback: SteemRepository.Callback<Boolean>) {
        Observable.fromCallable {
            val result = cancelVote(story.author, story.permlink)
            if (result) {
                return@fromCallable result
            } else {
                throw error("")
            }
        }
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    if (!it) {
                        Observable.empty<Boolean>()
                    }
                }
                .flatMap {
                    return@flatMap getStoryDetails(AccountName(story.author), Permlink(story.permlink), story.indexInResponse)
                }
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            val result = it
                            if (result?.discussion != null) {
                                val mapped = DiscussionToStoryMapper(result, steemJConfig?.defaultAccount?.name
                                        ?: "_").map()
                                if (mapped.isNotEmpty()) {
                                    mapped[0].storyType = type.ordinal
                                    RepositoryProvider.getDaoRepository().updateStorySync(mapped[0])
                                    callback.onSuccess(true)
                                    return@subscribe
                                }
                            }
                            callback.onSuccess(false)

                        },
                        {
                            callback.onError(it)
                        }
                )
    }

    override fun voteWithUpdate(story: StoryEntity, type: FeedType, percent: Int, callback: SteemRepository.Callback<Boolean>) {
//        Timber.d("voteWithUpdate(id=${story.entityId}; percent=$percent)")
        Observable.fromCallable {
            return@fromCallable vote(story.author, story.permlink, percent)
        }
                .subscribeOn(Schedulers.io())
                .flatMap {
//                    Timber.d("voteWithUpdate.flatMap(result=${it})")
                    return@flatMap getStoryDetails(AccountName(story.author), Permlink(story.permlink), story.indexInResponse)
                }
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            val result = it
                            if (result?.discussion != null) {
//                                Timber.d("voteWithUpdate.subscribe.onNext(discussion != null)")
                                val mapped = DiscussionToStoryMapper(result, steemJConfig?.defaultAccount?.name
                                        ?: "_").map()
                                if (mapped.isNotEmpty()) {
                                    mapped[0].storyType = type.ordinal
//                                    Timber.d("voteWithUpdate.subscribe.onNext(save StoryEntity into DB)")
                                    RepositoryProvider.getDaoRepository().updateStorySync(mapped[0])
                                    callback.onSuccess(true)
                                    return@subscribe
                                }
                            }
                            callback.onSuccess(false)

                        },
                        {
                            callback.onError(it)
                        }
                )
    }
}
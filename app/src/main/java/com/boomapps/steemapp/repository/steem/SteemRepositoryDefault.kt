/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

import android.net.Uri
import com.boomapps.steemapp.BuildConfig
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.db.DiscussionMapper
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import eu.bittrade.crypto.core.AddressFormatException
import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.DiscussionQuery
import eu.bittrade.libs.steemj.base.models.Permlink
import eu.bittrade.libs.steemj.base.models.operations.CommentOperation
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.apache.commons.lang3.tuple.ImmutablePair
import org.jetbrains.annotations.NotNull
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
    override fun login(nickname: String, postingKey: String?): SteemRepoResponse {
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
                return SteemRepoResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, null)
            }
        } catch (sce: SteemConnectionException) {
            Timber.e(sce)
            sce.printStackTrace()
            return SteemRepoResponse(false, SteemErrorCodes.CONNECTION_ERROR, null)
        } catch (sre: SteemResponseException) {
            Timber.e(sre)
            sre.printStackTrace()
            return SteemRepoResponse(false, SteemErrorCodes.TIMEOUT_ERROR, null)
        } catch (afe: AddressFormatException) {
            Timber.e(afe)
            afe.printStackTrace()
            return SteemRepoResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, null)
        } catch (ipe: InvalidParameterException) {
            Timber.e(ipe)
            ipe.printStackTrace()
            return SteemRepoResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR, ipe.message)
        } catch (ex: Exception) {
            Timber.e(ex)
            ex.printStackTrace()
            return SteemRepoResponse(false, SteemErrorCodes.UNDEFINED_ERROR, null)
        }
        return SteemRepoResponse(true, SteemErrorCodes.EMPTY_ERROR, null)
    }

    private fun addNewPostingKey(key: String) {
        steemJConfig?.privateKeyStorage?.addPrivateKeyToAccount(steemJConfig?.defaultAccount, ImmutablePair(PrivateKeyType.POSTING, key))
    }

    override fun post(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, permlink: String?): PostingResult {
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
                    return postWithKey(title, content, tags, rewardsPercent, upvote, permlink)
                } else {
                    return PostingResult(SteemAnswerCodes.EPTY_POSTING_KEY, "Posting key is empty.", false)
                }

            } else {
                return postWithKey(title, content, tags, rewardsPercent, upvote, permlink)
            }
        } catch (communicationException: SteemCommunicationException) {
            Timber.e(communicationException)
            return PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, communicationException.localizedMessage, false)
        } catch (responseException: SteemResponseException) {
            Timber.e(responseException)
            return PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, responseException.localizedMessage, false)
        } catch (transactionException: SteemInvalidTransactionException) {
            Timber.e(transactionException)
            return PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, transactionException.localizedMessage, false)
        } catch (parameterException: InvalidParameterException) {
            Timber.e(parameterException)
            return PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, parameterException.localizedMessage, false)
        } finally {
            return PostingResult(SteemAnswerCodes.SUCCESS, "", true)
        }
    }


    private fun postWithKey(@NotNull title: String, @NotNull content: String, @NotNull tags: Array<String>, rewardsPercent: Short, upvote: Boolean, permlink: String?): PostingResult {
        var commentOperation: CommentOperation? = null
        try {
            commentOperation = steemJ?.createPostSynchronous(title, content, tags, rewardsPercent, permlink)
            Timber.d("postWithKey: commentOperations.notNull == ${commentOperation != null}")

        } catch (e: SteemResponseException) {
            if (permlink != null) {
                // it was 2nd call with special permlink
                PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, "Cannot create new post. The same title post.\n Try to change your post title to another.", false)
            } else {
                if (e.data != null && e.data.has("code")) {
                    val names = e.data.fieldNames()
                    names.forEach {
                        if (it != null) {
                            Timber.d("Node $it; value is: ")
                            val node = e.data.get(it)
                            if (node.isInt) {
                                Timber.d(" Int -> ${node.intValue()}")
                            } else
                                if (node.isTextual) {
                                    Timber.d("Textual -> ${node.textValue()}")
                                } else {
                                    Timber.d("Other -> $node")
                                }

                        }

                    }
                    val codeNode = e.data.get("code")
                    if (codeNode.isInt) {
                        val codeValue = codeNode.intValue()
                        Timber.d("Posting error: code = $codeValue")
                        if (codeValue == 10 /*The permlink of a comment cannot change.*/) {
                            return PostingResult(SteemAnswerCodes.PERMLINK_DUPLICATE, "Cannot publish new post with the same title", false)
                        }
                    }
                }
            }

        }

        if (!upvote) {
            return PostingResult(SteemAnswerCodes.SUCCESS, "", true)
        }
        if (commentOperation != null) {
            val permlink = commentOperation.permlink.link
            steemJ?.vote(steemJConfig?.defaultAccount, Permlink(permlink), 100.toShort())
            return PostingResult(SteemAnswerCodes.SUCCESS, "", true)
        } else {
            return PostingResult(SteemAnswerCodes.UNKNOWN_ERROR, "No comment operation.", false)
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
        run {
            getStoryDiscussionTree(sortType, storyEntity?.permlink, storyEntity?.author, 0)
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


    fun getStoryDiscussionTree(sortType: DiscussionSortType, permlinkString: String?, authorName: String?, limit: Int) {
//        val dQuery = DiscussionQuery()
//        dQuery.parentAuthor = AccountName(authorName)
//        dQuery.parentPermlink = Permlink(permlinkString)
//        dQuery.limit = limit
        Timber.d("TREE: for $authorName :: $permlinkString")
        Single.fromCallable {
            return@fromCallable steemJ?.getRepliesByLastUpdate(AccountName(authorName), Permlink(permlinkString), 10)
        }
                .subscribeOn(Schedulers.io())
                .map {
                    Timber.d("TREE: number = ${it.size}")
                    val arrayList: ArrayList<DiscussionData> = arrayListOf()
                    for ((index, discussion) in it.withIndex()) {
                        arrayList.add(DiscussionData(index, discussion))
                    }
                    return@map arrayList
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null && it.size > 0) {
                        for (data in it) {
                            Timber.d("TREE: ${data.discussion?.rootTitle}")
                        }
                    }
                }, {

                })
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

    override fun cancelVote(author: String, postPermLink: String): Boolean {
        try {
            steemJ?.cancelVote(AccountName(author),
                    Permlink(postPermLink))
            return true
        } catch (communicationException: SteemCommunicationException) {
            Timber.e(communicationException)
            return false
        } catch (responseException: SteemResponseException) {
            Timber.e(responseException)
            return false
        } catch (transactionException: SteemInvalidTransactionException) {
            Timber.e(transactionException)
            return false
        }
        return false
    }

    override fun unvoteWithUpdate(story: StoryEntity, type: FeedType, callback: SteemRepository.Callback<Boolean>) {
        Observable.fromCallable {
            val result = cancelVote(story.author, story.permlink)
            Timber.d("unvoteWithUpdate >> result of cancelVote = $result")
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
                                val mapped = DiscussionMapper(result, steemJConfig?.defaultAccount?.name
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
                                val mapped = DiscussionMapper(result, steemJConfig?.defaultAccount?.name
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


    override fun loadStoryComments(entity: StoryEntity) {
        Timber.d("COMMENTS: loadStoryComments for ${entity.entityId}")
        val allComments: ArrayList<CommentEntity> = arrayListOf()

        Observable
                .fromCallable {
                    return@fromCallable loadSubComments(entity.entityId, entity.entityId,
                            entity.author, entity.permlink)
                }
                .doOnNext {
                    Timber.d("COMMENTS: Story : ${entity.title}")
                    Timber.d("COMMENTS: Story's children number: " + entity.commentsNum + " >>\n")
                    if (it != null && it.isNotEmpty()) {
                        logComments(it, 1)
                    }

                }
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it != null && it.isNotEmpty()) {
                                allComments.addAll(getOneLevelCommentsArray(it, 0))
                            }
                        },
                        {
                            // process error
                        },
                        {
                            // store into db
                            if (allComments.isNotEmpty()) {
                                Timber.d("COMMENTS: loadStoryComments >> input in DB ${allComments.size} comments")
                                RepositoryProvider.getDaoRepository().insertComments(allComments.toTypedArray())
                            }
                        })
    }

    private fun getOneLevelCommentsArray(ordered: Array<CommentsOrderedData>, level: Int): ArrayList<CommentEntity> {
        val result: ArrayList<CommentEntity> = arrayListOf()
        for (comment in ordered) {
            result.add(mapCommentIntoEntity(comment, level))
            if (comment.children.isNotEmpty()) {
                result.addAll(getOneLevelCommentsArray(comment.children, level + 1))
            }
        }
        return result
    }


    private fun mapCommentIntoEntity(raw: CommentsOrderedData, level: Int): CommentEntity {
        val entity = CommentEntity()
        entity.commentId = raw.data.id
        entity.rootId = raw.rootId
        entity.parentId = raw.parentId
        entity.author = raw.data.author.name
        entity.permlink = raw.data.permlink.link
        entity.title = raw.data.title
        entity.body = raw.data.body
        entity.level = level
        entity.order = levelWeight[level] + raw.order
        entity.votesNum = raw.data.getVotesNum()
        val activeVoters: ArrayList<String> = arrayListOf()
        if (raw.data.activeVotes.isNotEmpty()) {
            activeVoters.addAll(raw.data.activeVotes.map { it.voter.name })
        }
        entity.voters = activeVoters.toTypedArray()
        entity.price = raw.data.getUSDprice()
        entity.created = raw.data.created.dateTimeAsTimestamp
        entity.lastUpdate = raw.data.lastUpdate.dateTimeAsTimestamp
        entity.entityLastLoadTime = System.currentTimeMillis()
        return entity
    }

    fun loadSubComments(rootId: Long, parentId: Long, parentAuthor: String, parentPermlink: String):
            Array<CommentsOrderedData> {
        val response = steemJ?.getContentReplies(AccountName(parentAuthor), Permlink(parentPermlink))
        val result: ArrayList<CommentsOrderedData> = arrayListOf()
        if (response != null && response.size > 0) {
            var order: Int = 0
            for (discussion in response) {
                if (discussion == null) {
                    continue
                }
                val children: Array<CommentsOrderedData> = if (discussion.children > 0) {
                    loadSubComments(rootId, discussion.id, discussion.author.name, discussion.permlink.link)
                } else {
                    arrayOf()
                }
                result.add(CommentsOrderedData(rootId, parentId, order++, discussion, children))
            }
        }
        return result.toTypedArray()
    }

    private val levelWeight: Array<Long> = arrayOf(
            1000000000L,
            1100000000L,
            1110000000L,
            1111000000L,
            1111100000L,
            1111110000L,
            1111111000L,
            1111111100L
    )

    private fun logComments(allComments: Array<CommentsOrderedData>, level: Int) {
        val prefix = when (level) {
            1 -> "-- %s\n %d-%d"
            2 -> "---- %s\n %d-%d"
            3 -> "------ %s\n %d-%d"
            4 -> "-------- %s\n %d-%d"
            5 -> "---------- %s\n %d-%d"
            6 -> "------------ %s\n %d-%d"
            else -> "\n| %s\n %d-%d"
        }
        for (data in allComments) {
            Timber.d(prefix, data.data.body, data.rootId, data.parentId)
            if (data.children.isNotEmpty()) {
                logComments(data.children, level + 1)
            }
        }
    }


    data class CommentsOrderedData(val rootId: Long, val parentId: Long, val order: Int, val data: Discussion, var
    children: Array<CommentsOrderedData>) : Comparable<CommentsOrderedData> {

        override fun compareTo(other: CommentsOrderedData): Int {
            return data.created.dateTimeAsInt - other.data.created.dateTimeAsInt
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CommentsOrderedData

            if (rootId != other.rootId) return false
            if (parentId != other.parentId) return false
            if (data.author != other.data.author && data.permlink != other.data.permlink) return false

            return true
        }

        override fun hashCode(): Int {
            var result = rootId.hashCode()
            result = 31 * result + parentId.hashCode()
            result = 31 * result + data.author.hashCode() + data.permlink.hashCode()
            return result
        }
    }

}
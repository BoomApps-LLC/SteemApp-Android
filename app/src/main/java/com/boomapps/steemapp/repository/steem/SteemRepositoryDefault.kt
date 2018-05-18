package com.boomapps.steemapp.repository.steem

import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.BuildConfig
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.RepositoryProvider
import eu.bittrade.crypto.core.AddressFormatException
import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.Permlink
import eu.bittrade.libs.steemj.base.models.operations.CommentOperation
import eu.bittrade.libs.steemj.configuration.SteemJConfig
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

    /**
     *
     */
    override fun login(nickname: String, postingKey: String?): SteemWorkerResponse {
        Timber.log(Log.INFO, "login($nickname, $postingKey")
        try {
            steemJConfig = SteemJConfig.getInstance()
            steemJConfig?.setAppName("SteemApp")
            steemJConfig?.setAppVersion(BuildConfig.VERSION_NAME)

//        steemJConfig?.addEndpointURI(URI.create("https://api.steemit.com"))
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


    override fun post(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean): SteemWorker.PostingResult {
        try {
            steemJConfig?.privateKeyStorage
            if (steemJConfig?.privateKeyStorage?.privateKeysPerAccounts == null || steemJConfig?.privateKeyStorage?.privateKeysPerAccounts?.size == 0) {
                if (postingKey.isNotEmpty()) {
                    steemJConfig?.privateKeyStorage?.addPrivateKeyToAccount(steemJConfig?.defaultAccount, ImmutablePair(PrivateKeyType.POSTING, postingKey))
                    val commentOperation: CommentOperation? = steemJ?.createPost(title, content, tags, rewardsPercent)
                    if (commentOperation != null) {
                        val metadata = commentOperation.jsonMetadata
                        Timber.d("post answer >> ${metadata?.toString()}")
                    }
                } else {
                    return SteemWorker.PostingResult("Posting key is empty.", false)
                }
            } else {
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

    override fun uploadImage(uri: Uri): URL? {
        val imageFile = File(uri.path)
        if (!imageFile.exists() || imageFile.isDirectory || !imageFile.canRead()) {
            return URL("http://empty.com")
        }
        val config = SteemJImageUploadConfig.getInstance()
        config.connectTimeout = 30000
        config.readTimeout = 30000
        val uData = RepositoryProvider.instance.getSharedRepository().loadUserData()
        var url: URL? = null
        try {
            url = SteemJImageUpload.uploadImage(
                    eu.bittrade.libs.steemj.image.upload.models.AccountName(uData.nickname),
                    uData.postKey,
                    imageFile)
        } catch (e: IOException) {
            Timber.e(e, "Image upload exception")
        }
        return url
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
        Timber.d("getStoryDetails >> ${aName?.name}")
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.FEED, steemJ, aName).load() }
    }


    override fun getFeedShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        Timber.d("getStoryDetails >> ${aName?.name}")
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.FEED, steemJ, aName, start, limit).load() }
    }

    override fun getBlogShortDataList(aName: AccountName?): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        Timber.d("getStoryDetails >> ${aName?.name}")
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.BLOG, steemJ, aName).load() }
    }

    override fun getBlogShortDataList(aName: AccountName?, start: Int, limit: Int): Observable<ArrayList<StoryShortEntry>> {
        val aName = if (aName != null) {
            aName
        } else {
            steemJConfig?.defaultAccount
        }
        Timber.d("getStoryDetails >> ${aName?.name}")
        return Observable.fromCallable { return@fromCallable FeedShortEntriesRequest(FeedType.BLOG, steemJ, aName, start, limit).load() }
    }

    override fun getStoryDetails(aName: AccountName?, pLink: Permlink, orderId : Int): Observable<DiscussionData> {
        val rqName = aName ?: steemJConfig?.defaultAccount
        Timber.d("getStoryDetails >> ${rqName?.name} :: ${pLink.link}")
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
}
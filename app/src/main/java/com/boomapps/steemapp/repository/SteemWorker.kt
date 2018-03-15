package com.boomapps.steemapp.repository

import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.BuildConfig
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.repository.entity.profile.ProfileMetadata
import com.google.gson.Gson
import eu.bittrade.crypto.core.AddressFormatException
import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry
import eu.bittrade.libs.steemj.apis.follow.model.CommentBlogEntry
import eu.bittrade.libs.steemj.apis.follow.model.CommentFeedEntry
import eu.bittrade.libs.steemj.apis.follow.model.FeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.base.models.Permlink
import eu.bittrade.libs.steemj.base.models.Price
import eu.bittrade.libs.steemj.base.models.operations.CommentOperation
import eu.bittrade.libs.steemj.configuration.SteemJConfig
import eu.bittrade.libs.steemj.enums.PrivateKeyType
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemConnectionException
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import eu.bittrade.libs.steemj.image.upload.SteemJImageUpload
import eu.bittrade.libs.steemj.image.upload.config.SteemJImageUploadConfig
import org.apache.commons.lang3.tuple.ImmutablePair
import java.io.File
import java.net.URL
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by vgrechikha on 22.01.2018.
 */

class SteemWorker() {

    private var steemJ: SteemJ? = null
    private var steemJConfig: SteemJConfig? = null

    companion object {
        private var instance: SteemWorker = SteemWorker()
        val LOG_TAG = SteemWorker::class.java.simpleName

        fun get(): SteemWorker {
            return instance
        }
    }

    init {
        instance = this
    }

    fun isLogged(): Boolean {
        return steemJ != null && steemJConfig != null
    }


    fun login(nickname: String, postingKey: String?, activeKey: String?): SteemWorkerResponse {
        Log.d(LOG_TAG, "login(${nickname}, ${postingKey}")
        steemJConfig = SteemJConfig.getInstance()
        steemJConfig?.setAppName("SteemApp")
        steemJConfig?.setAppVersion(BuildConfig.VERSION_NAME)

//        steemJConfig?.addEndpointURI(URI.create("https://api.steemit.com"))
        steemJConfig?.defaultAccount = AccountName(nickname)
        steemJConfig?.responseTimeout = 10000;
        try {
            val privateKeys = arrayListOf<ImmutablePair<PrivateKeyType, String>>()
            if (postingKey != null && postingKey.isNotEmpty()) {
                privateKeys.add(ImmutablePair(PrivateKeyType.POSTING, postingKey))
            }
            if (activeKey != null && activeKey.isNotEmpty()) {
                privateKeys.add(ImmutablePair(PrivateKeyType.ACTIVE, activeKey))
            }
            if (privateKeys.size > 0) {
                steemJConfig?.privateKeyStorage?.addAccount(steemJConfig?.defaultAccount, privateKeys);
            } else {
                steemJConfig?.privateKeyStorage?.addAccount(steemJConfig?.defaultAccount);
            }

            steemJ = SteemJ()
            var result = steemJ?.lookupAccounts(nickname, 1)
            if(result == null || result.size == 0 || result[0].toLowerCase() != nickname.toLowerCase()){
                return SteemWorkerResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR)
            }
        } catch (sce: SteemConnectionException) {
            Log.d(LOG_TAG, "Login Error : ${sce.localizedMessage}")
            sce.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.CONNECTION_ERROR)
        } catch (sre: SteemResponseException) {
            Log.d(LOG_TAG, "Login Error : ${sre.localizedMessage}")
            sre.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.TIMEOUT_ERROR)
        } catch (afe: AddressFormatException) {
            Log.d(LOG_TAG, "Login Error : key is not valid >> ${afe.localizedMessage}")
            afe.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.INCORRECT_USER_DATA_ERROR)
        } catch (ex: Exception) {
            Log.d(LOG_TAG, "Login Error : global catcher >> ${ex.localizedMessage}")
            ex.printStackTrace()
            return SteemWorkerResponse(false, SteemErrorCodes.UNDEFINED_ERROR)
        }
        return SteemWorkerResponse(true, SteemErrorCodes.EMPTY_ERROR)
    }

    fun getProfile(nickname: String): UserData {
        val extAccounts = steemJ?.getAccounts(listOf(AccountName(nickname)))
        if (extAccounts != null && extAccounts.size > 0) {
            // get only first
            val acc = extAccounts.get(0)
            if (acc != null) {
                val metadata = acc.jsonMetadata
                Log.d("getProfile", "metadata=${metadata}")
                val gson = Gson()
                val parsed = gson.fromJson<ProfileMetadata>(metadata, ProfileMetadata::class.java)
                val d = acc.balance.toReal()
                Log.d("getProfile", "balance=${d}")
                return UserData(
                        nickname,
                        parsed?.profileEntity?.userName,
                        parsed?.profileEntity?.photoUrl,
                        "")
            }
        }
        return SharedRepository().loadUserData()
    }

    fun uploadPhoto() {

    }

    fun signOut() {
        steemJConfig = null
        steemJ = null
    }


    fun post(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean): PostingResult {
        try {
            steemJConfig?.privateKeyStorage
            if (steemJConfig?.privateKeyStorage?.privateKeysPerAccounts == null || steemJConfig?.privateKeyStorage?.privateKeysPerAccounts?.size == 0) {
                if (postingKey.isNotEmpty()) {
                    steemJConfig?.privateKeyStorage?.addPrivateKeyToAccount(steemJConfig?.defaultAccount, ImmutablePair(PrivateKeyType.POSTING, postingKey))
                    val commentOperation: CommentOperation? = steemJ?.createPost(title, content, tags, rewardsPercent)
                    if (commentOperation != null) {
                        val metadata = commentOperation.jsonMetadata
                        Log.d("SteemWorker", "post answer >> ${metadata?.toString()}")
                    }
                } else {
                    return PostingResult("Posting key is empty.", false)
                }
            } else {
                val commentOperation = steemJ?.createPostSynchronous(title, content, tags, rewardsPercent)
                if (!upvote) {
                    return PostingResult()
                }
                if (commentOperation != null) {
                    val permlink = commentOperation.permlink.link
                    steemJ?.vote(steemJConfig?.defaultAccount, Permlink(permlink), 100.toShort())
                    return PostingResult()
                } else {
                    return PostingResult("No comment operation.", false)
                }
            }
        } catch (communicationException: SteemCommunicationException) {
            Log.e("SteemWorker", "post error SteemCommunicationException >> ${communicationException.localizedMessage}", communicationException)
            return PostingResult(communicationException.localizedMessage, false)
        } catch (responseException: SteemResponseException) {
            Log.e("SteemWorker", "post error SteemResponseException >>${responseException.localizedMessage}", responseException)
            return PostingResult(responseException.localizedMessage, false)
        } catch (transactionException: SteemInvalidTransactionException) {
            Log.e("SteemWorker", "post error SteemInvalidTransactionException >> ${transactionException.localizedMessage}", transactionException)
            return PostingResult(transactionException.localizedMessage, false)
        } catch (parameterException: InvalidParameterException) {
            Log.e("SteemWorker", "post error InvalidParameterException >> ${parameterException.localizedMessage}", parameterException)
            return PostingResult(parameterException.localizedMessage, false)
        }
        return PostingResult()
    }

    fun uploadImage(uri: Uri): URL {
        val imageFile = File(uri.path)
        if (!imageFile.exists() || imageFile.isDirectory || !imageFile.canRead()) {
            return URL("http://empty.com")
        }
        val config = SteemJImageUploadConfig.getInstance()
        config.connectTimeout = 3000
        config.readTimeout = 3000
        val uData = SharedRepository().loadUserData()
        return SteemJImageUpload.uploadImage(
                eu.bittrade.libs.steemj.image.upload.models.AccountName(uData.nickname),
                uData.postKey,
                imageFile)
    }


    /*
         * Upvote the post
         * "steem-java-api-learned-to-speak-graphene-update-5" written by
         * "dez1337" using 100% of the defaultAccounts voting power.
         */
    fun vote(postPermLink: String, percentage: Int) {
        try {
            steemJ?.vote(steemJConfig?.apiUsername, Permlink(postPermLink),
                    percentage.toShort())
        } catch (communicationException: SteemCommunicationException) {

        } catch (responseException: SteemResponseException) {

        } catch (transactionException: SteemInvalidTransactionException) {

        }
    }

    fun cancelVote(postPermLink: String) {
        try {
            steemJ?.cancelVote(steemJConfig?.apiUsername,
                    Permlink(postPermLink))
        } catch (communicationException: SteemCommunicationException) {

        } catch (responseException: SteemResponseException) {

        } catch (transactionException: SteemInvalidTransactionException) {

        }
    }

    fun getCurrentPrice() {
        val price = steemJ?.getCurrentMedianHistoryPrice()?.getBase()?.getAmount()
        Log.d(LOG_TAG, "Current Price is ${price}")
    }


    fun getVestingShares(): Array<Double> {
        val dgProperties = steemJ?.dynamicGlobalProperties
        val tFund = dgProperties?.totalVestingFundSteem?.toReal()
        val tShares = dgProperties?.totalVestingShares?.toReal()
        if (tFund == null || tShares == null) {
            return arrayOf()
        } else {
            return arrayOf(tFund, tShares)
        }
//        val userNames = listOf<AccountName>(AccountName("yuriks2000"))
//        val listOfUsers = steemJ?.getAccounts(userNames)
//        val userExtended = listOfUsers?.get(0)
//        val rewardSteems = userExtended?.rewardSteemBalance?.toReal()
//        val fullBalance = userExtended?.balance?.toReal()
//        Log.d(LOG_TAG, "for userExtended yurics2000 balances are : ${fullBalance} & ${rewardSteems}")
//        if (userExtended != null && tFund != null && tShares != null) {
//            val userVests = userExtended.vestingShares?.toReal()
//            val steemPower = tFund * (userVests!!.div(tShares))
//            Log.d(LOG_TAG, "Calculated steemPower = " + steemPower)
//            return steemPower
//        } else {
//            return BigDecimal.ZERO
//        }
    }


    fun test() {
        Log.d(LOG_TAG, "SteemWorker start test")
        val count = steemJ?.accountCount
        Log.d(LOG_TAG, "count = ${count}")
        val volume = steemJ?.volume
        val steemVolume = volume?.steemVolume
        Log.d(LOG_TAG, "STEEM volume =${steemVolume?.toReal()} ${steemVolume?.symbol?.name}")
        val sbdVolume = volume?.sbdVolume
        Log.d(LOG_TAG, "SBD volume =${sbdVolume?.toReal()} ${sbdVolume?.symbol?.name}")
        val feedsHistory = steemJ?.feedHistory
        Log.d(LOG_TAG, "feeds currentPrice=${feedsHistory?.currentPrice}; ")
        if (feedsHistory != null) {
            val feedEntries = steemJ?.getFeedEntries(steemJConfig?.defaultAccount, feedsHistory.id, 2)
            if (feedEntries != null && feedEntries.size > 0) {
                for (entry in feedEntries) {
                    printFeedEntry(entry)
                    Log.d(LOG_TAG, "     : COMMENTS ====================================== START")
                    val commentFeedEntry = steemJ?.getFeed(steemJConfig?.defaultAccount, entry.entryId, 2)
                    commentFeedEntry?.forEach {
                        printComment(it)
                        val blog = steemJ?.getBlog(steemJConfig?.defaultAccount, it.entryId, 2)
                        blog?.forEach { printBlog(it) }
                    }
                }
            }
        }
        val blogEntries = steemJ?.getBlogEntries(steemJConfig?.defaultAccount, 0, 2)
        if (blogEntries != null && blogEntries.size > 0) {
            blogEntries.forEach { printBlogEntry(it) }
        }
        val historyPrices = feedsHistory?.priceHistory
        if (historyPrices != null && historyPrices.size > 0) {
            for (price: Price in historyPrices) {
                Log.d(LOG_TAG, "feedPrice: base=${price.base.toReal()}; quote=${price.quote.toReal()}")
            }
        }
    }

    private fun printFeedEntry(entry: FeedEntry) {
        Log.d(LOG_TAG, "ENTRY: ====================================== START")
        Log.d(LOG_TAG, "     : id = ${entry.entryId}")
        Log.d(LOG_TAG, "     : author = ${entry.author.name}")
        Log.d(LOG_TAG, "     : permlink = ${entry.permlink.link}")
        Log.d(LOG_TAG, "     : reblogOn = ${SimpleDateFormat("hh:mm dd.MM.yyyy").format(Date(entry.reblogOn.dateTimeAsTimestamp))}")
        Log.d(LOG_TAG, "     : ====================================== FINISH")
    }

    private fun printComment(commentFeedEntry: CommentFeedEntry) {
        Log.d(LOG_TAG, "                 >> from ${commentFeedEntry.comment.author.name} in category ${commentFeedEntry.comment.category}")
        Log.d(LOG_TAG, "                 >> json = ${commentFeedEntry.comment.jsonMetadata}")
    }

    private fun printBlogEntry(bEntry: BlogEntry) {
        Log.d(LOG_TAG, "BLOG ENTRY: ====================================== START")
        Log.d(LOG_TAG, "     : id = ${bEntry.entryId}")
        Log.d(LOG_TAG, "     : author = ${bEntry.author.name}")
        Log.d(LOG_TAG, "     : permlink = ${bEntry.permlink.link}")
    }

    private fun printBlog(commentBlogEntry: CommentBlogEntry?) {
        Log.d(LOG_TAG, "        BLOG: ====================================== ")
        if (commentBlogEntry == null) {
            Log.d(LOG_TAG, "            : empty")
        } else {
            Log.d(LOG_TAG, "            : json=${commentBlogEntry.comment.jsonMetadata}")
        }

    }

    data class PostingResult(var result: String = "Posting was successful", var success: Boolean = true)

}
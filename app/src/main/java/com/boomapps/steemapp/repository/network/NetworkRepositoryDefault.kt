/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.network

import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.repository.HeadersInterceptor
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.RequestsApi
import com.boomapps.steemapp.repository.currency.AmountRequestData
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.repository.currency.TradingPairInfo
import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.boomapps.steemapp.repository.entity.profile.ProfileMetadataDeserializer
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.entity.profile.UserExtended
import com.boomapps.steemapp.repository.feed.FeedFullData
import com.boomapps.steemapp.repository.feed.ServerDate
import com.boomapps.steemapp.repository.feed.ServerDateSerialiizer
import com.boomapps.steemapp.repository.steem.SteemAnswerCodes
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class NetworkRepositoryDefault : NetworkRepository {

    override var extendedProfileResponse: ProfileResponse? = null


    companion object {
        var instance: NetworkRepositoryDefault = NetworkRepositoryDefault()
        lateinit var httpClient: OkHttpClient
        fun get(): NetworkRepositoryDefault {
            return instance
        }
    }


    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)  // <-- this is the important line!
        httpClientBuilder.cookieJar(object : CookieJar {
            private var cookieStore: HashMap<HttpUrl, List<Cookie>> = hashMapOf();

            override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                cookieStore.put(url, cookies);
            }

            override fun loadForRequest(url: HttpUrl?): MutableList<Cookie> {
                var cookies: MutableList<Cookie>? = cookieStore.get(url)?.toMutableList();
                return if (cookies != null) {
                    cookies
                } else {
                    arrayListOf<Cookie>()
                };
            }
        })
        NetworkRepositoryDefault.httpClient = httpClientBuilder.build()
    }


    override fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, callback: NetworkRepository.OnRequestFinishCallback<Any?>) {
        postStory(title, content, tags, postingKey, rewardsPercent, upvote, null, callback)
    }

    override fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, permlink: String?, callback: NetworkRepository.OnRequestFinishCallback<Any?>) {
        Observable.fromCallable {
            return@fromCallable RepositoryProvider.getSteemRepository().post(title, content, tags, postingKey, rewardsPercent, upvote, permlink)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.success) {
                        callback.onSuccessRequestFinish("")
                    } else {
                        if (it.code == SteemAnswerCodes.PERMLINK_DUPLICATE) {
                            callback.onFailureRequestFinish(NetworkResponseCode.PERMLINK_DUPLICATE, Throwable(it.result))
                        } else {
                            callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, Throwable(it.result))
                        }

                    }
                    Log.d("postStory", "doOnNext")
                }
                .doOnComplete {
                    Log.d("postStory", "doOnComplete")
                }
                .doOnError {
                    Log.d("postStory", "doOnError")
                    if (it is TimeoutException || it is IOException) {
                        callback.onFailureRequestFinish(NetworkResponseCode.CONNECTION_ERROR, it)
                    } else {
                        callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, it)
                    }
                }
                .subscribe()
    }

    override fun uploadNewPhoto(uri: Uri, callback: NetworkRepository.OnRequestFinishCallback<URL?>) {
        var result: URL? = null
        Observable.fromCallable {
            return@fromCallable RepositoryProvider.getSteemRepository().uploadImage(uri)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d("uploadNewPhoto", "doOnNext: ${it.toString()}")
                    result = it
                }
                .doOnComplete {
                    Log.d("uploadNewPhoto", "doOnComplete")
                    if (result != null && !result.toString().equals("http://empty.com", true)) {
                        callback.onSuccessRequestFinish(result)
                    } else {
                        callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, Throwable("Image uploading error"))
                    }
                }
                .doOnError {
                    Timber.e(it)
                    callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, it)
                    if (it is TimeoutException || it is IOException) {
                        callback.onFailureRequestFinish(NetworkResponseCode.CONNECTION_ERROR, it)
                    } else {
                        callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, it)
                    }
                }
                .subscribe()
    }

    override fun loadExtendedUserProfile(nick: String, callback: NetworkRepository.OnRequestFinishCallback<ProfileResponse?>) {
        if (extendedProfileResponse != null) {
            callback.onSuccessRequestFinish(extendedProfileResponse)
            return
        }
        // update current currencies
        getRequestsApi("https://steemit.com/", null).loadProfileExtendedData(nick)
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { contributor ->
                            extendedProfileResponse = contributor
                            callback.onSuccessRequestFinish(extendedProfileResponse)

                        },
                        { throwable ->
                            if (throwable is TimeoutException || throwable is IOException) {
                                callback.onFailureRequestFinish(NetworkResponseCode.CONNECTION_ERROR, throwable)
                            } else {
                                callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, throwable)
                            }

                        }
                )
    }

    override fun loadFullStartData(nick: String, callback: NetworkRepository.OnRequestFinishCallback<Any?>) {
        val coinmarketcapToUsdFloawable: Observable<Array<CoinmarketcapCurrency>> = getObservableForCurrency("steem")//.onErrorResumeNext { s: Subscriber<in Array<CoinmarketcapCurrency>>? -> Log.d("NetworkRepoDef", "steemToUsd error loading") }
        val coinmarketcapDollarToUsdFlowable: Observable<Array<CoinmarketcapCurrency>> = getObservableForCurrency("steem-dollars")//.onErrorResumeNext { s: Subscriber<in Array<CoinmarketcapCurrency>>? -> Log.d("NetworkRepoDef", "steemDollarToUsd error loading") }
        val balanceVestFlowable: Observable<Array<Double>> = getBalanceVetstObservable()//.onErrorResumeNext { s: Subscriber<in Array<Double>>? -> Log.d("NetworkRepoDef", "balanceVests error loading") }
        val exUserData: Observable<ProfileResponse> = getRequestsApi("https://steemit.com/", null).loadProfileExtendedData(nick)

        Observable.combineLatestDelayError(arrayOf(
                coinmarketcapToUsdFloawable,
                coinmarketcapDollarToUsdFlowable,
                balanceVestFlowable,
                exUserData),
                {

                    var toUSD = CoinmarketcapCurrency("steem")
                    var dollarToUsd = CoinmarketcapCurrency("steem-dollars")
                    var balanceVest: Array<Double> = arrayOf()
                    var profile = ProfileResponse()

                    for (obj in it) {
                        when (obj) {
                            is Array<*> -> {
                                if (obj.isNotEmpty()) {
                                    when (obj.get(0)) {
                                        is CoinmarketcapCurrency -> {
                                            val currency = (obj[0] as CoinmarketcapCurrency)
                                            if (currency.id.toLowerCase() == "steem") {
                                                toUSD = currency
                                            } else {
                                                dollarToUsd = currency
                                            }
                                        }
                                        is Double -> {
                                            balanceVest = obj as Array<Double>
                                        }
                                    }
                                }
                            }
                            is ProfileResponse -> {
                                profile = obj
                            }
                        }
                    }
                    saveData(toUSD, dollarToUsd, profile, balanceVest)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onExceptionResumeNext {
                    Log.d("NetworkRepository", "loadFullStartData >> onExceptionResumeNext")
                    callback.onSuccessRequestFinish("")
                }
                .doOnError {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnError")
//                    callback.onSuccessRequestFinish()
                }.doOnComplete {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnComplete")
                    callback.onSuccessRequestFinish("")
                }
                .subscribe()

    }


    fun saveData(su: CoinmarketcapCurrency, sdu: CoinmarketcapCurrency, pr: ProfileResponse, bv: Array<Double>) {
        if (su.currencyName.isNotEmpty()) {
            RepositoryProvider.getPreferencesRepository().saveSteemCurrency(su)
        }
        if (sdu.currencyName.isNotEmpty()) {
            RepositoryProvider.getPreferencesRepository().saveSBDCurrency(sdu)
        }
        if (pr.userExtended != null) {
            RepositoryProvider.getPreferencesRepository().saveUserExtendedData(pr.userExtended as UserExtended)
        }
        Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> su = ${su.usdPrice}")
        Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> sdu = ${sdu.usdPrice}")
        if (bv.size == 2) {
            Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> bv[0] = ${bv[0]}")
            Log.d("NetworkRepository", "loadFullStartData:: combineFunction >> bv[1] = ${bv[1]}")
            RepositoryProvider.getPreferencesRepository().saveTotalVestingData(bv)
        }
    }


    private fun getBalanceVetstObservable(): Observable<Array<Double>> {
        return Observable.fromCallable {
            return@fromCallable RepositoryProvider.getSteemRepository().getVestingShares()
        }
    }


    private fun getObservableForCurrency(currencyName: String): Observable<Array<CoinmarketcapCurrency>> {
        return getRequestsApi("https://api.coinmarketcap.com/v1/ticker/", null).loadCurrencyFor(currencyName)
    }

    private fun getRequestsApi(basePoint: String, headers: Map<String, String>?): RequestsApi {
        val localBuilder: OkHttpClient.Builder = NetworkRepositoryDefault.httpClient.newBuilder()
        if (headers != null) {
            localBuilder.addNetworkInterceptor(HeadersInterceptor(headers))
        }


        localBuilder.connectTimeout(10, TimeUnit.SECONDS)
        localBuilder.readTimeout(10, TimeUnit.SECONDS)
        localBuilder.retryOnConnectionFailure(false)
        val gson = GsonBuilder()
                .registerTypeAdapter(UserDataEntity::class.java, ProfileMetadataDeserializer())
                .registerTypeAdapter(ServerDate::class.java, ServerDateSerialiizer())
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(basePoint)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(localBuilder.build())
                .build()
        return retrofit.create(RequestsApi::class.java)
    }


    override fun loadExtendedPostData(name: String, permlink: String): Observable<FeedFullData> {
        return getRequestsApi("https://steemit.com/", null).loadFeedFullData(name, permlink)
    }

    override fun loadTradingPairs(callback: NetworkRepository.OnRequestFinishCallback<Array<TradingPairInfo>>) {
        getRequestsApi("https://blocktrades.us:443/api/v2/", null)
                .loadAllTradingPairs()
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { contributor ->
                            callback.onSuccessRequestFinish(contributor ?: arrayOf())
                        },
                        { throwable ->
                            if (throwable is TimeoutException || throwable is IOException) {
                                callback.onFailureRequestFinish(NetworkResponseCode.CONNECTION_ERROR, throwable)
                            } else {
                                callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, throwable)
                            }

                        }
                )
    }

    override fun loadOutputAmount(requestData : AmountRequestData, callback: NetworkRepository.OnRequestFinishCallback<OutputAmount>) {
        val params = hashMapOf<String, String>()
        params["inputAmount"] = requestData.value.toString()
        params["inputCoinType"] = requestData.inputType
        params["outputCoinType"] = requestData.outputType
        getRequestsApi("https://blocktrades.us:443/api/v2/", null)
                .getOutputAmount(params)
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { contributor ->
                            callback.onSuccessRequestFinish(contributor ?: OutputAmount())

                        },
                        { throwable ->
                            if (throwable is TimeoutException || throwable is IOException) {
                                callback.onFailureRequestFinish(NetworkResponseCode.CONNECTION_ERROR, throwable)
                            } else {
                                callback.onFailureRequestFinish(NetworkResponseCode.UNKNOWN_ERROR, throwable)
                            }

                        }
                )
    }
}
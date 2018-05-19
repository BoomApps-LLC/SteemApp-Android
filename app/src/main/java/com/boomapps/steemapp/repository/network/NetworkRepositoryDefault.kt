package com.boomapps.steemapp.repository.network

import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.repository.HeadersInterceptor
import com.boomapps.steemapp.repository.RequestsApi
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.boomapps.steemapp.repository.entity.profile.ProfileMetadataDeserializer
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.entity.profile.UserExtended
import com.boomapps.steemapp.repository.feed.FeedFullData
import com.boomapps.steemapp.repository.feed.ServerDate
import com.boomapps.steemapp.repository.feed.ServerDateSerialiizer
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
import java.net.URL
import java.util.concurrent.TimeUnit

class NetworkRepositoryDefault : NetworkRepository {

    override var extendedProfileResponse: ProfileResponse? = null
    override var coinmarketcapCurrency: CoinmarketcapCurrency? = null
    override var lastUploadedPhotoUrl: URL? = null

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


    override fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, callback: NetworkRepository.OnRequestFinishCallback) {
        Observable.fromCallable {
            return@fromCallable ServiceLocator.getSteemRepository().post(title, content, tags, postingKey, rewardsPercent, upvote)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it.success) {
                        callback.onSuccessRequestFinish()
                    } else {
                        callback.onFailureRequestFinish(Throwable(it.result))
                    }
                    Log.d("postStory", "doOnNext")
                }
                .doOnComplete {
                    Log.d("postStory", "doOnComplete")
                }
                .doOnError {
                    Log.d("postStory", "doOnError")
                    callback.onFailureRequestFinish(it)
                }
                .subscribe()
    }

    override fun uploadNewPhoto(uri: Uri, callback: NetworkRepository.OnRequestFinishCallback) {
        Observable.fromCallable {
            return@fromCallable ServiceLocator.getSteemRepository().uploadImage(uri)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d("uploadNewPhoto", "doOnNext: ${it.toString()}")
                    lastUploadedPhotoUrl = it
                }
                .doOnComplete {
                    Log.d("uploadNewPhoto", "doOnComplete")
                    if (lastUploadedPhotoUrl != null && !lastUploadedPhotoUrl.toString().equals("http://empty.com", true)) {
                        callback.onSuccessRequestFinish()
                    } else {
                        callback.onFailureRequestFinish(Throwable("Image uploading error"))
                    }
                }
                .doOnError {
                    Log.d("uploadNewPhoto", "doOnError")
                    callback.onFailureRequestFinish(it)

                }
                .subscribe()
    }

    override fun loadExtendedUserProfile(nick: String, callback: NetworkRepository.OnRequestFinishCallback) {
        if (extendedProfileResponse != null) {
            callback.onSuccessRequestFinish()
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
                            callback.onSuccessRequestFinish()

                        },
                        { throwable ->
                            callback.onFailureRequestFinish(throwable)
                        }
                )
    }

    override fun loadFullStartData(nick: String, callback: NetworkRepository.OnRequestFinishCallback) {
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
                    callback.onSuccessRequestFinish()
                }
                .doOnError {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnError")
//                    callback.onSuccessRequestFinish()
                }.doOnComplete {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnComplete")
                    callback.onSuccessRequestFinish()
                }
                .subscribe()

    }


    fun saveData(su: CoinmarketcapCurrency, sdu: CoinmarketcapCurrency, pr: ProfileResponse, bv: Array<Double>) {
        if (su.currencyName.isNotEmpty()) {
            ServiceLocator.getPreferencesRepository().saveSteemCurrency(su)
        }
        if (sdu.currencyName.isNotEmpty()) {
            ServiceLocator.getPreferencesRepository().saveSBDCurrency(sdu)
        }
        if (pr.userExtended != null) {
            ServiceLocator.getPreferencesRepository().saveUserExtendedData(pr.userExtended as UserExtended)
        }
        Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> su = ${su.usdPrice}")
        Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> sdu = ${sdu.usdPrice}")
        if (bv.size == 2) {
            Log.d("NetworkRepoDef", "loadFullStartData:: combineFunction >> bv[0] = ${bv[0]}")
            Log.d("NetworkRepository", "loadFullStartData:: combineFunction >> bv[1] = ${bv[1]}")
            ServiceLocator.getPreferencesRepository().saveTotalVestingData(bv)
        }
    }


    private fun getBalanceVetstObservable(): Observable<Array<Double>> {
        return Observable.fromCallable {
            return@fromCallable ServiceLocator.getSteemRepository().getVestingShares()
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


    override fun loadExtendedPostData(name : String, permlink : String) : Observable<FeedFullData>{
        return getRequestsApi("https://steemit.com/", null).loadFeedFullData(name, permlink)
    }

    private fun loadFeedFullData() {

    }

}
package com.boomapps.steemapp.repository

import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.boomapps.steemapp.repository.entity.profile.ProfileMetadataDeserializer
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.entity.profile.UserExtended
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit


/**
 * Created by Vitali Grechikha on 03.02.2018.
 */
class NetworkRepository {

    interface OnRequestFinishCallback {

        fun onSuccessRequestFinish()

        fun onFailureRequestFinish(throwable: Throwable)

    }

    companion object {
        var extendedProfileResponse: ProfileResponse? = null
        var coinmarketcapCurrency: CoinmarketcapCurrency? = null
        var instance: NetworkRepository = NetworkRepository()
        lateinit var httpClient: OkHttpClient
        fun get(): NetworkRepository {
            return instance
        }
    }

    var lastUploadedPhotoUrl: URL? = null

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
        httpClient = httpClientBuilder.build()
    }


    fun postStory(title: String, content: String, tags: Array<String>, postingKey: String, rewardsPercent: Short, upvote: Boolean, callback: OnRequestFinishCallback) {
        Flowable.fromCallable {
            return@fromCallable SteemWorker.get().post(title, content, tags, postingKey, rewardsPercent, upvote)
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

    fun uploadNewPhoto(uri: Uri, callback: OnRequestFinishCallback) {
//        val file = Utils().getFileFromUri(uri)
//        if (file == null) {
//            Log.d("uploadNewPhoto", "file is absent")
//            return
//        } else {
//            Log.d("uploadNewPhoto", "file is ${file.absolutePath}")
//        }
        Flowable.fromCallable {
            return@fromCallable SteemWorker.get().uploadImage(uri)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d("uploadNewPhoto", "doOnNext: ${it.toString()}")
                    // TODO check nullable url
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


    fun getFlowableForCurrency(currencyName: String): Flowable<Array<CoinmarketcapCurrency>> {
        return getRequestsApi("https://api.coinmarketcap.com/v1/ticker/", null).loadCurrencyFor(currencyName)
    }


    fun loadExtendedUserProfile(nick: String, callback: OnRequestFinishCallback) {
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


    fun loadFullStartData(nick: String, callback: OnRequestFinishCallback) {
        val coinmarketcapToUsdFloawable: Flowable<Array<CoinmarketcapCurrency>> = getFlowableForCurrency("steem")//.onErrorResumeNext { s: Subscriber<in Array<CoinmarketcapCurrency>>? -> Log.d("NetworkRepository", "steemToUsd error loading") }
        val coinmarketcapDollarToUsdFlowable: Flowable<Array<CoinmarketcapCurrency>> = getFlowableForCurrency("steem-dollars")//.onErrorResumeNext { s: Subscriber<in Array<SteemDollarCurrency>>? -> Log.d("NetworkRepository", "steemDollarToUsd error loading") }
        val balanceVestFlowable: Flowable<Array<Double>> = getBalanceVetstFlowable()//.onErrorResumeNext { s: Subscriber<in BigDecimal>? -> Log.d("NetworkRepository", "balanceVests error loading") }
        val exUserData: Flowable<ProfileResponse> = getRequestsApi("https://steemit.com/", null).loadProfileExtendedData(nick)

        val disposable: Disposable = Flowables.combineLatest(
                coinmarketcapToUsdFloawable,
                coinmarketcapDollarToUsdFlowable,
                exUserData,
                balanceVestFlowable,
                { su, sdu, pr, bv ->
                    if (su.isNotEmpty()) {
                        Storage.get().setSteemCurrency(su[0])
                    }
                    if (sdu.isNotEmpty()) {
                        Storage.get().setSBDCurrency(sdu[0])
                    }
                    if (pr.userExtended != null) {
                        Storage.get().setUserExtended(pr.userExtended as UserExtended)
                    }
                    Storage.get().setSBDCurrency(sdu[0])
                    Log.d("NetworkRepository", "doTest:: combineFunction >> su = ${su[0].usdPrice}")
                    Log.d("NetworkRepository", "doTest:: combineFunction >> sdu = ${sdu[0].usdPrice}")
                    if (bv.size == 2) {
                        Log.d("NetworkRepository", "doTest:: combineFunction >> bv[0] = ${bv[0]}")
                        Log.d("NetworkRepository", "doTest:: combineFunction >> bv[1] = ${bv[1]}")
                        Storage.get().setTotalVestingData(bv)
                    }
                }
        ).timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnComplete")
                    callback.onSuccessRequestFinish()
                }
                .doOnError {
                    Log.d("NetworkRepository", "loadFullStartData >> doOnError")
                    callback.onFailureRequestFinish(it)
                }
                .subscribe()
    }


    fun getBalanceVetstFlowable(): Flowable<Array<Double>> {
        return Flowable.fromCallable {
            return@fromCallable SteemWorker.get().getVestingShares()
        }
    }

// TODO use one method and entity for currency of steem dollar and steem

    fun getSteemCurrencyFlowable(): Flowable<Array<CoinmarketcapCurrency>> {
        return Flowable.fromCallable {
            return@fromCallable requestSteemCurrency()
        }
    }


    private fun requestSteemCurrency(): Array<CoinmarketcapCurrency> {
        val gson = Gson()
        val url = URL("https://api.coinmarketcap.com/v1/ticker/steem/")
        val reader = InputStreamReader(url.openStream())
        val currencyArray = gson.fromJson(reader, Array<CoinmarketcapCurrency>::class.java)
        reader.close()
        return currencyArray
    }

    private fun getRequestsApi(basePoint: String, headers: Map<String, String>?): RequestsApi {
        val localBuilder: OkHttpClient.Builder = httpClient.newBuilder()
        if (headers != null) {
            localBuilder.addNetworkInterceptor(HeadersInterceptor(headers))
        }


        localBuilder.connectTimeout(10, TimeUnit.SECONDS)
        localBuilder.readTimeout(10, TimeUnit.SECONDS)
        localBuilder.retryOnConnectionFailure(false)
        val gson = GsonBuilder()
                .registerTypeAdapter(UserDataEntity::class.java, ProfileMetadataDeserializer())
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl(basePoint)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(localBuilder.build())
                .build()
        return retrofit.create(RequestsApi::class.java)
    }

    val headersInterceptor: Interceptor = object : Interceptor {

        override fun intercept(chain: Interceptor.Chain?): Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


}
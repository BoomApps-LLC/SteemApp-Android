package com.boomapps.steemapp.repository

import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Vitali Grechikha on 03.02.2018.
 */
interface RequestsApi {


    @GET("@{userExtended}.json")
    fun loadProfileExtendedData(@Path("userExtended") user: String): Observable<ProfileResponse>

    @GET("{currency_name}")
    fun loadCurrencyFor(@Path("currency_name") user: String): Observable<Array<CoinmarketcapCurrency>>

}
package com.boomapps.steemapp.repository.currency

import com.google.gson.annotations.SerializedName

/**
 * Created by Vitali Grechikha on 04.02.2018.
 */
class CoinmarketcapCurrency(serverId: String) {
    //    {
//        "id": "steem",
//        "name": "Steem",
//        "symbol": "STEEM",
//        "rank": "26",
//        "price_usd": "3.84808",
//        "price_btc": "0.00045782",
//        "24h_volume_usd": "44724600.0",
//        "market_cap_usd": "956121949.0",
//        "available_supply": "248467274.0",
//        "total_supply": "265441368.0",
//        "max_supply": null,
//        "percent_change_1h": "-4.56",
//        "percent_change_24h": "-11.92",
//        "percent_change_7d": "-38.48",
//        "last_updated": "1517761450"
//    }

    @SerializedName("id")
    var id: String = serverId

    @SerializedName("name")
    var currencyName: String = ""

    @SerializedName("price_usd")
    var usdPrice: Double = 1.0

    @SerializedName("last_updated")
    var lastUpdateTime: Long = 0L


}
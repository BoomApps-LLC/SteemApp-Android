package com.boomapps.steemapp.repository.currency

import com.google.gson.annotations.SerializedName

class TradingPairInfo {

    @SerializedName("inputCoinType")
    var inputCoin : String = ""

    @SerializedName("outputCoinType")
    var outputCoin : String = ""
}
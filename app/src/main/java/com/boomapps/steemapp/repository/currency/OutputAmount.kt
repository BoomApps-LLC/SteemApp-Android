package com.boomapps.steemapp.repository.currency

import com.google.gson.annotations.SerializedName

class OutputAmount {

    /*{
        "inputAmount": "1",
        "inputCoinType": "steem",
        "outputAmount": "1.0739",
        "outputCoinType": "bitusd"
    }*/


    @SerializedName("inputAmount")
    var inputAmount: Float = 0.0f

    @SerializedName("inputCoinType")
    var inputCoinType : String = "steem"

    @SerializedName("outputAmount")
    var outputAmount : Float = 0.0f

    @SerializedName("outputCoinType")
    var outputCoinType : String = "bitusd"

}
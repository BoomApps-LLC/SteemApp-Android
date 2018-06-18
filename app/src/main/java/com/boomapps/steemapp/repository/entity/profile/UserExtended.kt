/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.entity.profile

import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.google.gson.annotations.SerializedName

/**
 * Created by Vitali Grechikha on 11.02.2018.
 */
class UserExtended {

    @SerializedName("id")
    var id: Long = 0L

    @SerializedName("name")
    var name: String = ""

    @SerializedName("balance") // + as STEEM
    var steemBalance: String = "0.000 STEEM"

    @SerializedName("savings_balance") // + as STEEM
    var steemSavingBalance: String = "0.000 STEEM"

    @SerializedName("sbd_balance") // + as SDB
    var sbdBalance: String = "0.000 SBD"

    @SerializedName("savings_sbd_balance") // + as SDB
    var sbdSavingBalance: String = "0.000 SBD"


    @SerializedName("reward_sbd_balance")
    var sdbRewardBalance: String = "0.000 SBD"

    @SerializedName("reward_steem_balance")
    var steemRewardBalance: String = "0.000 STEEM"

    @SerializedName("reward_vesting_balance")
    var vestRewardBalance: String = "0.000000 VESTS"

    @SerializedName("vesting_shares") // + as VEST
    var vestShares: String = "0.000000 VESTS"

    @SerializedName("json_metadata")
    var profileMetadata: UserDataEntity = UserDataEntity()

}
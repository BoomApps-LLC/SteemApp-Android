package com.boomapps.steemapp.repository.entity.profile

import com.google.gson.annotations.SerializedName

/**
 * Created by Vitali Grechikha on 03.02.2018.
 */
class ProfileResponse {

    @SerializedName("user")
    var userExtended: UserExtended? = null

    @SerializedName("status")
    var statusCode = ""

}
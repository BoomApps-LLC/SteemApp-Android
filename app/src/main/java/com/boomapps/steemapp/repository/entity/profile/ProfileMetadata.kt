package com.boomapps.steemapp.repository.entity.profile

import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.google.gson.annotations.SerializedName

/**
 * Created by Vitali Grechikha on 28.01.2018.
 */
class ProfileMetadata {

    @SerializedName("profile")
    var profileEntity: UserDataEntity = UserDataEntity()
}
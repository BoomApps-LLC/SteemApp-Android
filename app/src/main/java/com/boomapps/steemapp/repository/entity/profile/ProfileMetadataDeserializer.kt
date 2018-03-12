package com.boomapps.steemapp.repository.entity.profile

import com.boomapps.steemapp.repository.entity.UserDataEntity
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Created by vgrechikha on 28.02.2018.
 */
class ProfileMetadataDeserializer : JsonDeserializer<UserDataEntity> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UserDataEntity {
        if (json.isJsonObject) {
            val jObject = json.asJsonObject
            if (jObject.has("profile")) {
                val jProfile = jObject.getAsJsonObject("profile")
                val data = UserDataEntity()
                if (jProfile.has("profile_image")) {
                    data.photoUrl = jProfile.getAsJsonPrimitive("profile_image").asString
                }
                if (jProfile.has("name")) {
                    data.userName = jProfile.getAsJsonPrimitive("name").asString
                }
                if (jProfile.has("about")) {
                    data.userAbout = jProfile.getAsJsonPrimitive("about").asString
                }
                if (jProfile.has("location")) {
                    data.location = jProfile.getAsJsonPrimitive("location").asString
                }
                return data
            }
        }
        return UserDataEntity()
    }
}
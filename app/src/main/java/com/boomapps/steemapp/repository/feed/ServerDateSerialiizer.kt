package com.boomapps.steemapp.repository.feed

import com.google.api.client.json.JsonString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ServerDateSerialiizer : JsonDeserializer<ServerDate> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ServerDate {
        if(json != null && json.isJsonPrimitive){
            val sDate = json.asString
            return ServerDate(sDate)
        }
        return ServerDate("1970-01-01T00:00:00")
    }
}
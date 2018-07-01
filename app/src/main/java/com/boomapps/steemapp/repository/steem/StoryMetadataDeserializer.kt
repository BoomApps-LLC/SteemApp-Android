package com.boomapps.steemapp.repository.steem

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import timber.log.Timber
import java.lang.reflect.Type

class StoryMetadataDeserializer : JsonDeserializer<StoryMetadata> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StoryMetadata {
        Timber.d("deserialize")
        val result = StoryMetadata()
        if (json.isJsonObject) {
            val jObject = json.asJsonObject
            if (jObject.has("tags")) {
                if (jObject.get("tags").isJsonArray) {
                    result.tags = jObject.getAsJsonArray("tags").map { it.asString }.toTypedArray()
                } else {
                    result.tags = arrayOf(jObject.getAsJsonPrimitive("tags").asString)
                }
            }
            if (jObject.has("links")) {
                if (jObject.get("links").isJsonArray) {
                    result.links = jObject.getAsJsonArray("links").map { it.asString }.toTypedArray()
                } else {
                    result.links = arrayOf(jObject.getAsJsonPrimitive("links").asString)
                }
            }
            if (jObject.has("image")) {
                if (jObject.get("image").isJsonArray) {
                    result.imagesUrl = jObject.getAsJsonArray("image").map { it.asString }.toTypedArray()
                } else {
                    result.imagesUrl = arrayOf(jObject.getAsJsonPrimitive("image").asString)
                }
            }
            if (jObject.has("users")) {
                if (jObject.get("users").isJsonArray) {
                    result.users = jObject.getAsJsonArray("users").map { it.asString }.toTypedArray()
                } else {
                    result.users = arrayOf(jObject.getAsJsonPrimitive("users").asString)
                }
            }
        }
        return result
    }
}
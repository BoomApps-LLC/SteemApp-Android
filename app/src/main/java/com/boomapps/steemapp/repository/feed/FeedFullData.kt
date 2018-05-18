package com.boomapps.steemapp.repository.feed

import com.google.gson.annotations.SerializedName

class FeedFullData {

    @SerializedName("id")
    var id: Long = 0
    @SerializedName("author")
    var author: String = ""
    @SerializedName("permlink")
    var permlink: String = ""
    @SerializedName("category")
    var category: String = ""
    @SerializedName("parent_author")
    var parentAuthor: String = ""
    @SerializedName("parent_permlink")
    var parentPermlink: String = ""
    @SerializedName("title")
    var title: String = ""
    @SerializedName("body")
    var body: String = ""
    @SerializedName("last_update")
    var timeLastUpdate = ServerDate("1970-01-01T00:00:00")
    @SerializedName("created")
    var timeCreated = ServerDate("1970-01-01T00:00:00")
    @SerializedName("active")
    var timeActive = ServerDate("1970-01-01T00:00:00")
    @SerializedName("depth")
    var depth: Int = 0
    @SerializedName("children")
    var children: Int = 0
    @SerializedName("net_votes")
    var votes = 0
    @SerializedName("url")
    var rawUrl = ""
    @SerializedName("json_metadata")
    var metadata: FeedMetadata = FeedMetadata()

}
/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

import com.google.gson.annotations.SerializedName

class StoryMetadata {

    @SerializedName("tags")
    var tags: Array<String> = arrayOf()

    @SerializedName("users")
    var users: Array<String> = arrayOf()

    @SerializedName("image")
    var imagesUrl: Array<String> = arrayOf()

    @SerializedName("links")
    var links: Array<String> = arrayOf()

}
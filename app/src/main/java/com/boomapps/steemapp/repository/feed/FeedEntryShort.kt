package com.boomapps.steemapp.repository.feed

data class FeedEntryShort(val id: Int, val author: String, val permlink: String, val reblogBy: Array<String>, val reblogOnTime: Long)
package com.boomapps.steemapp.repository.blog

data class BlogEntryShort(val id: Int, val author: String, val permlink: String, val blogName: String, val reblogOnTime: Long)
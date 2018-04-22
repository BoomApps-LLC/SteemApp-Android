package com.boomapps.steemapp.feeds

data class FeedCardViewData(
        val imgUrl: String,
        val title: String,
        val text: String,
        val author: String,
        val avatarUrl: String,
        val timeAgo: String,
        val fullCounter: String,
        val fullPrice: String,
        val votesNum: Int,
        val commentsNum: Int,
        val linksNum: Int)
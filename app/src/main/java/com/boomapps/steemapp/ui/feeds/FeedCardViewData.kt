package com.boomapps.steemapp.ui.feeds

import com.boomapps.steemapp.repository.db.entities.BaseStoryEntity
import com.boomapps.steemapp.repository.db.entities.BlogEntity
import com.boomapps.steemapp.repository.feed.FeedMetadata
import com.google.gson.GsonBuilder

class FeedCardViewData() {

    val pattern = Regex("(\\s+)|(\\\\n+)")

    constructor(entity: BaseStoryEntity) : this() {
        id = 0
        title = entity.title
        text = entity.shortText
        author = entity.author
        votesNum = entity.votesNum
        commentsNum = entity.commentsNum
        imgUrl = entity.mainImageUrl
        avatarUrl = entity.avatarUrl
    }

    var id: Int = -1
    var imgUrl: String = ""
    var title: String = ""
    var text: String = ""
    var author: String = ""
    var avatarUrl: String = ""
    var timeAgo: Long = 0L
    var fullCounter: Int = 0
    var fullPrice: Float = 0.0f
    var votesNum: Int = 0
    var commentsNum: Int = 0
    var linksNum: Int = 0
    var permlink: String = ""



}
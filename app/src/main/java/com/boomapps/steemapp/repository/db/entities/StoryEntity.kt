/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db.entities

import android.arch.persistence.room.*

@Entity(tableName = "story_entities", indices = [(Index(value = ["story_type", "permlink"], unique = false))])
class StoryEntity {

    @PrimaryKey
    @ColumnInfo(name = "story_entity_id")
    var entityId: Long = 0
    var author: String = ""
    var permlink: String = ""
    var url: String = ""
    var title: String = ""
    @ColumnInfo(name = "raw_body")
    var rawBody: String = ""
    @ColumnInfo(name = "short_text")
    var shortText = ""
    @ColumnInfo(name = "image_url")
    var mainImageUrl = ""
    var category: String = ""
    var links: Array<String> = arrayOf()
    @ColumnInfo(name = "votes_num")
    var votesNum: Int = 0
    @ColumnInfo(name = "comments_num")
    var commentsNum: Int = 0
    @ColumnInfo(name = "links_num")
    var linksNum: Int = 0
    var images: Array<String> = arrayOf()
    var tags: Array<String> = arrayOf()
    var users: Array<String> = arrayOf()
    var type: Int = 0
    @ColumnInfo(name = "avatar")
    var avatarUrl: String = ""
    @ColumnInfo(name = "time_last_update")
    var lastUpdate: Long = 0L
    @ColumnInfo(name = "time_created")
    var created: Long = 0L
    @ColumnInfo(name = "time_active")
    var active: Long = 0L

    @ColumnInfo(name = "price")
    var price = 0.0f

    @ColumnInfo(name = "reputation")
    var reputation = 0

    @ColumnInfo(name = "story_type")
    var storyType: Int = -1

    @ColumnInfo(name = "index_in")
    var indexInResponse = -1

    @ColumnInfo(name = "is_voted")
    var isVoted = false

    @ColumnInfo(name = "vote_percent")
    var votePervecnt = 0

    @ColumnInfo(name = "vote_date")
    var voteDate = 0L

    @Ignore
    var fullCounter = 0

}
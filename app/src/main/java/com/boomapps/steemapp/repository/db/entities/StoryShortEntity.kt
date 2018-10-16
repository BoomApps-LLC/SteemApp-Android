/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_short_entities")
class StoryShortEntity {

    @PrimaryKey(autoGenerate = true)
    var id: Long = -1L

    @ColumnInfo(name = "entry_id")
    var entryId: Int = -1

    @ColumnInfo(name = "entry_type")
    var entryType: Int = -1

    @ColumnInfo(name = "author")
    var authorName: String = ""

    @ColumnInfo(name = "permlink")
    var permlink: String = ""

    @ColumnInfo(name = "time_point")
    var timePoint: Long = 0L

    companion object {
        fun create(eId: Int, type: Int, author: String, link: String, timePoint: Long): StoryShortEntity {
            val instance = StoryShortEntity()
            instance.entryId = eId
            instance.entryType = type
            instance.authorName = author
            instance.permlink = link
            instance.timePoint = timePoint
            return instance
        }
    }

}
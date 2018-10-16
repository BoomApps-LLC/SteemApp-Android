package com.boomapps.steemapp.repository.db.entities

import androidx.room.*


@Entity(tableName = "comments", indices = [(Index(value = ["root_id", "permlink", "author"], unique = false))])
class CommentEntity {

    @PrimaryKey
    @ColumnInfo(name = "comment_id")
    var commentId: Long = 0

    @ColumnInfo(name = "root_id")
    var rootId: Long = 0

    @ColumnInfo(name = "parent_id")
    var parentId: Long = 0

    var author: String = ""

    var permlink: String = ""

    var title: String = ""

    var body: String = ""

    var level: Int = 0

    var order: Int = 0

    @ColumnInfo(name = "votes_num")
    var votesNum: Int = 0

    // TODO use after implementing users DB
    @Ignore
    var voters: Array<String> = arrayOf()

    @ColumnInfo(name = "replies_num")
    var repliesNum: Int = 0

    // TODO use after implementing users DB
    @Ignore
    var repliers: Array<String> = arrayOf()

    @ColumnInfo(name = "price")
    var price = 0.0f

    @ColumnInfo(name = "time_last_update")
    var lastUpdate: Long = 0L

    @ColumnInfo(name = "time_created")
    var created: Long = 0L

    @ColumnInfo(name = "comment_last_time_load")
    var entityLastLoadTime: Long = 0L

    @ColumnInfo(name = "children_num")
    var childrenNum = 0

    @ColumnInfo(name = "is_voted")
    var isVoted = false

    @ColumnInfo(name = "vote_percent")
    var votePercent = 0

    @ColumnInfo(name = "vote_date")
    var voteDate = 0L


}
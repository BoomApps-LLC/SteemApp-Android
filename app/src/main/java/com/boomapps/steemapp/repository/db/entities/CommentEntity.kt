package com.boomapps.steemapp.repository.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "comments", indices = [(Index(value = ["root_id", "permlink", "author"], unique = false))])
class CommentEntity {

  @PrimaryKey
  @ColumnInfo(name = "comment_id")
  var commentId: Long = 0

  @ColumnInfo(name = "root_id")
  var rootId : Long = 0

  @ColumnInfo(name = "parent_id")
  var parentId : Long = 0

  var author: String = ""

  var permlink: String = ""

  var title: String = ""

  var body : String = ""

  var level : Int = 0

  var order : Int = 0

  @ColumnInfo(name = "votes_id")
  var votesNum : Int = 0

  var voters : Array<String> = arrayOf()

  @ColumnInfo(name = "price")
  var price = 0.0f

  @ColumnInfo(name = "time_last_update")
  var lastUpdate: Long = 0L

  @ColumnInfo(name = "time_created")
  var created: Long = 0L

  @ColumnInfo(name = "comment_last_time_load")
  var entityLastLoadTime : Long = 0L


}
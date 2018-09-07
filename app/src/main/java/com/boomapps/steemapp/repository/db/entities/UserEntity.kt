package com.boomapps.steemapp.repository.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

// TODO use name as foreign key in other entities;
// TODO look at {@url='https://developer.android.com/training/data-storage/room/defining-data'}

@Entity(tableName = "users", indices = [(Index(value = ["name", "user_id"], unique = true))])
class UserEntity {

  @PrimaryKey
  @ColumnInfo(name = "user_id")
  var userId : Long = 0L

  var name : String = ""

  @ColumnInfo(name = "avatar_url")
  var avatarUrl : String = ""

  var reputation : Int = 0

  @ColumnInfo(name = "last_online_time")
  var lastTimeOnline : Long = 0L

}
/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.boomapps.steemapp.repository.db.converters.Converters
import com.boomapps.steemapp.repository.db.dao.CommentsDao
import com.boomapps.steemapp.repository.db.dao.PostsDao
import com.boomapps.steemapp.repository.db.dao.StoriesDao
import com.boomapps.steemapp.repository.db.dao.UserDao
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.db.entities.UserEntity

@Database(entities = [StoryEntity::class, PostEntity::class, CommentEntity::class, UserEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun storiesDao(): StoriesDao

  abstract fun postsDao(): PostsDao

  abstract fun commentsDao(): CommentsDao

  abstract fun usersDao(): UserDao


  companion object {
    // provide static access to migration objects
    @JvmField
    val MIGRATION_1_2 = Migration1To2()
  }
}
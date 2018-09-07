package com.boomapps.steemapp.repository.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

/**
 * Migration class from 1st to 2nd version
 */
class Migration1To2 : Migration(1, 2) {

  override fun migrate(database: SupportSQLiteDatabase) {
    // create 'comments' table
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `comments` (`comment_id` INTEGER NOT NULL, `root_id` INTEGER NOT NULL, `parent_id` INTEGER NOT NULL, `author` TEXT NOT NULL, `permlink` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `level` INTEGER NOT NULL, `order` INTEGER NOT NULL, `votes_id` INTEGER NOT NULL, `voters` TEXT NOT NULL, `price` REAL NOT NULL, `time_last_update` INTEGER NOT NULL, `time_created` INTEGER NOT NULL, `comment_last_time_load` INTEGER NOT NULL, PRIMARY KEY(`comment_id`))"
    )
// create 'users' table
    database.execSQL(
        "CREATE TABLE IF NOT EXISTS `users` (`user_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `avatar_url` TEXT NOT NULL, `reputation` INTEGER NOT NULL, `last_online_time` INTEGER NOT NULL, PRIMARY KEY(`user_id`))"
    )
  }
}
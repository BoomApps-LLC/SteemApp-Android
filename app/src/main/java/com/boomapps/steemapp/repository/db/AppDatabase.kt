package com.boomapps.steemapp.repository.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.boomapps.steemapp.repository.db.converters.Converters
import com.boomapps.steemapp.repository.db.dao.StoriesDao
import com.boomapps.steemapp.repository.db.entities.StoryEntity

@Database(entities = [StoryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storiesDao(): StoriesDao

}
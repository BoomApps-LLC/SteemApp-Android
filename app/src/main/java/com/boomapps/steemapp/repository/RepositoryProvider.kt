/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository

import android.app.Application
import android.arch.persistence.room.Room
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.repository.db.AppDatabase
import com.boomapps.steemapp.repository.db.DaoRepository
import com.boomapps.steemapp.repository.db.DaoRepositoryDefault
import com.boomapps.steemapp.repository.files.FilesRepository
import com.boomapps.steemapp.repository.files.FilesRepositoryDefault
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkRepositoryDefault
import com.boomapps.steemapp.repository.preferences.SharedRepository
import com.boomapps.steemapp.repository.preferences.SharedRepositoryDefault
import com.boomapps.steemapp.repository.steem.SteemRepository
import com.boomapps.steemapp.repository.steem.SteemRepositoryDefault
import java.util.concurrent.Executors

interface RepositoryProvider {
  companion object {
    const val NETWORK_PAGE_SIZE = 12
    const val DATABASE_PAGE_SIZE = 10

    val locator: RepositoryProvider by lazy {
      DefaultRepositoryProvider(
          SteemApplication.instance as Application
      )
    }

    fun getDaoRepository(): DaoRepository {
      return locator!!.getDbRepository()
    }

    fun getSteemRepository(): SteemRepository {
      return locator!!.getSteemRepository()
    }

    fun getNetworkRepository(): NetworkRepository {
      return locator!!.getNetworkRepository()
    }

    fun getPreferencesRepository(): SharedRepository {
      return locator!!.getPreferencesRepository()
    }

    fun getFileRepository(): FilesRepository {
      return locator!!.getFileRepository()
    }
  }

//    /**
//     * Allows tests to replace the default implementations.
//     */
//    @VisibleForTesting
//    fun swap(newLocator: RepositoryProvider) {
//        locator = newLocator
//    }

  fun getDbRepository(): DaoRepository

  fun getSteemRepository(): SteemRepository

  fun getNetworkRepository(): NetworkRepository

  fun getPreferencesRepository(): SharedRepository

  fun getFileRepository(): FilesRepository

}

open class DefaultRepositoryProvider(val app: Application) : RepositoryProvider {
  private val dbRepo by lazy {
    DaoRepositoryDefault(
        Room.databaseBuilder(app, AppDatabase::class.java, "steem_app_db")
            .addMigrations(AppDatabase.MIGRATION_1_2) // add migration for room
            .build(),
        Executors.newSingleThreadExecutor()
    )
  }

  private val steemRepo by lazy {
    SteemRepositoryDefault()
  }

  private val networkRepo by lazy {
    NetworkRepositoryDefault()
  }

  private val sharedRepo by lazy {
    SharedRepositoryDefault()
  }

  private val fileRepo by lazy {
    FilesRepositoryDefault()
  }

  override fun getDbRepository(): DaoRepository {
    return dbRepo
  }

  override fun getSteemRepository(): SteemRepository {
    return steemRepo
  }

  override fun getNetworkRepository(): NetworkRepository {
    return networkRepo
  }

  override fun getPreferencesRepository(): SharedRepository {
    return sharedRepo
  }

  override fun getFileRepository(): FilesRepository {
    return fileRepo
  }
}
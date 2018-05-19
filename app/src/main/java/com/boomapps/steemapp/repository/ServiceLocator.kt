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

interface ServiceLocator {
    companion object {
        const val NETWORK_PAGE_SIZE = 15
        const val DATABASE_PAGE_SIZE = 10

        val locator: ServiceLocator by lazy {
            DefaultServiceLocator(
                    SteemApplication.instance as Application)
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
//    fun swap(newLocator: ServiceLocator) {
//        locator = newLocator
//    }

    fun getDbRepository(): DaoRepository

    fun getSteemRepository(): SteemRepository

    fun getNetworkRepository(): NetworkRepository

    fun getPreferencesRepository(): SharedRepository

    fun getFileRepository(): FilesRepository

}

open class DefaultServiceLocator(val app: Application) : ServiceLocator {
    private val dbRepo by lazy {
        DaoRepositoryDefault(
                Room.databaseBuilder(app, AppDatabase::class.java, "steem_app_db").build(),
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
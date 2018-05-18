package com.boomapps.steemapp.repository

import android.app.Application
import android.arch.persistence.room.Room
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.repository.db.AppDatabase
import com.boomapps.steemapp.repository.db.DaoRepository
import com.boomapps.steemapp.repository.db.DaoRepositoryDefault
import com.boomapps.steemapp.repository.steem.SteemRepository
import com.boomapps.steemapp.repository.steem.SteemRepositoryDefault
import java.util.concurrent.Executors

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
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

    override fun getDbRepository(): DaoRepository {
        return dbRepo
    }

    override fun getSteemRepository(): SteemRepository {
        return steemRepo
    }
}
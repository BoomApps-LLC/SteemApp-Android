package com.boomapps.steemapp.repository

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

/**
 * Created by vgrechikha on 21.03.2018.
 */
class RepositoryProvider {
    constructor() :
            this(
                    NetworkRepositoryDefault(),
                    SharedRepositoryDefault(),
                    FilesRepositoryDefault(),
                    SteemRepositoryDefault(),
                    DaoRepositoryDefault()
            )

    constructor(
            networkRepo: NetworkRepository,
            sharedRepo: SharedRepository,
            filesRepo: FilesRepository,
            steemRepo: SteemRepository,
            daoRepo: DaoRepository
    ) {
        networkRepository = networkRepo
        sharedRepository = sharedRepo
        filesRepository = filesRepo
        steemRepository = steemRepo
        daoRepository = daoRepo
    }


    companion object {
        var instance: RepositoryProvider = RepositoryProvider()

    }

    private var networkRepository: NetworkRepository
    private var sharedRepository: SharedRepository
    private var filesRepository: FilesRepository
    private var steemRepository: SteemRepository
    private var daoRepository: DaoRepository

    fun getNetworkRepository(): NetworkRepository {
        return networkRepository
    }

    fun getSharedRepository(): SharedRepository {
        return sharedRepository
    }

    fun getFileRepository(): FilesRepository {
        return filesRepository
    }

    fun getSteemRepository(): SteemRepository {
        return steemRepository
    }

    fun getDaoRepository(): DaoRepository {
        return daoRepository
    }

    fun setNetworkRepository(repository: NetworkRepository) {
        networkRepository = repository
    }

    fun setPreferencesRepository(repository: SharedRepository) {
        sharedRepository = repository
    }

    fun setFilesRepository(repository: FilesRepository) {
        filesRepository = repository
    }

    fun setSteemRepository(repository: SteemRepository) {
        steemRepository = repository
    }

}
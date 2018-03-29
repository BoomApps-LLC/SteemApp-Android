package com.boomapps.steemapp.repository

import com.boomapps.steemapp.repository.files.FilesRepository
import com.boomapps.steemapp.repository.files.FilesRepositoryDefault
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkRepositoryDeafult
import com.boomapps.steemapp.repository.preferences.SharedRepository
import com.boomapps.steemapp.repository.preferences.SharedRepositoryDefault

/**
 * Created by vgrechikha on 21.03.2018.
 */
class RepositoryProvider {

    companion object {
        val instance = RepositoryProvider()
    }

    private var networkRepository: NetworkRepository = NetworkRepositoryDeafult()
    private var sharedRepository: SharedRepository = SharedRepositoryDefault()
    private var filesRepository: FilesRepository = FilesRepositoryDefault()

    fun getNetworkRepository(): NetworkRepository {
        return networkRepository
    }

    fun getSharedRepository(): SharedRepository {
        return sharedRepository
    }

    fun getFileRepository(): FilesRepository {
        return filesRepository
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


}
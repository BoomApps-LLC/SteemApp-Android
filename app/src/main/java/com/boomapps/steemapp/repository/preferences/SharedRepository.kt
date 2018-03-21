package com.boomapps.steemapp.repository.preferences

import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.main.Balance
import com.boomapps.steemapp.repository.StoryInstance

/**
 * Created by vgrechikha on 21.03.2018.
 */
interface SharedRepository {

    fun saveUserData(userData: UserData)

    fun updateUserData(userData: UserData)

    fun loadUserData(): UserData

    fun isUserLogged(): Boolean

    fun saveBalanceData(balance: Balance?)

    fun loadBalance(): Balance

    fun saveStoryData(storyInstance: StoryInstance)

    fun loadStoryData(): StoryInstance

    fun saveLastTimePosting(currentTimeMillis: Long)

    fun loadLastTimePosting(): Long

    fun isFirstLaunch(): Boolean

    fun setFirstLaunchState(isFirst : Boolean)

}
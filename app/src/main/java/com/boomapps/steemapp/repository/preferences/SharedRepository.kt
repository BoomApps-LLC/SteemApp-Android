package com.boomapps.steemapp.repository.preferences

import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.repository.StoryInstance
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.VoteState
import com.boomapps.steemapp.repository.entity.profile.UserExtended

/**
 * Created by vgrechikha on 21.03.2018.
 */
interface SharedRepository {

    fun saveUserData(userData: UserData)

    fun updateUserData(userData: UserData)

    fun loadUserData(): UserData

    fun isUserLogged(): Boolean

    fun saveBalanceData(balance: Balance?)

    fun loadBalance(recalculate: Boolean): Balance

    fun saveStoryData(storyInstance: StoryInstance)

    fun loadStoryData(): StoryInstance

    fun saveLastTimePosting(currentTimeMillis: Long)

    fun loadLastTimePosting(): Long

    fun isFirstLaunch(): Boolean

    fun setFirstLaunchState(isFirst: Boolean)

    fun saveSteemCurrency(currency: CoinmarketcapCurrency)

    fun saveSBDCurrency(currency: CoinmarketcapCurrency)

    fun saveUserExtendedData(data: UserExtended)

    fun saveTotalVestingData(data: Array<Double>)

    fun clearAllData()

    fun saveSuccessfulPostingNumber(num: Int)

    fun loadSuccessfulPostingNumber(): Int

    fun saveVotingState(state: VoteState)

    fun loadVotingState(): VoteState

}
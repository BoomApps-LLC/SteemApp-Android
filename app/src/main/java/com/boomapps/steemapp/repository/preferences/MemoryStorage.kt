package com.boomapps.steemapp.repository.preferences

import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.profile.UserExtended

/**
 * Created by Vitali Grechikha on 13.02.2018.
 */
class MemoryStorage {

    private var currencySteem: CoinmarketcapCurrency = CoinmarketcapCurrency("steem")
    private var currencySBD: CoinmarketcapCurrency = CoinmarketcapCurrency("steem-dollars")
    private var extUserData: UserExtended? = null
    private var shortUserInfo: UserData? = null
    /**
     * contains 2 values:
     * [0] - total_vesting_fund_steem
     * [1] - total_vesting_shares
     */
    private var totalVestingData: Array<Double> = arrayOf()


    fun getSteemCurrency(): CoinmarketcapCurrency {
        return currencySteem
    }

    fun getSbdCurrency(): CoinmarketcapCurrency {
        return currencySBD
    }

    fun setSteemCurrency(value: CoinmarketcapCurrency) {
        currencySteem = value
    }

    fun setSBDCurrency(value: CoinmarketcapCurrency) {
        currencySBD = value
    }

    fun getUserExtendedData(): UserExtended {
        if (extUserData == null) {
            extUserData = UserExtended()
        }
        return extUserData!!
    }

    fun setUserExtended(userExtended: UserExtended) {
        extUserData = userExtended
        // update short user data
        val oldData = shortUserInfo
        val newName = userExtended.profileMetadata.userName
        val newPhotoUrl = userExtended.profileMetadata.photoUrl
        shortUserInfo = UserData(
                userExtended.name,
                if (newName.isNotEmpty()) {
                    newName
                } else {
                    oldData?.userName
                },
                if (newPhotoUrl.isNotEmpty()) {
                    newPhotoUrl
                } else {
                    oldData?.photoUrl
                },
                oldData?.postKey)
        if (extUserData != null) {
            val newUserData = UserData(shortUserInfo?.nickname, userExtended.profileMetadata.userName, userExtended.profileMetadata.photoUrl, shortUserInfo?.postKey)
            RepositoryProvider.instance.getSharedRepository().updateUserData(newUserData)
        }
    }

    fun getTotalVestingData(): Array<Double> {
        return totalVestingData
    }

    fun setTotalVestingData(values: Array<Double>) {
        totalVestingData = values
    }

    fun clearAllData() {
        extUserData = null
        shortUserInfo = null
    }


}
package com.boomapps.steemapp.repository

import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.main.Balance
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.profile.UserExtended

/**
 * Created by Vitali Grechikha on 13.02.2018.
 */
class Storage {

    companion object {
        var storage: Storage = Storage()
        fun get(): Storage {
            return storage
        }
    }


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

    fun getShortUserData(): UserData {
        if (shortUserInfo == null) {
            shortUserInfo = SharedRepository().loadUserData()
        }
        return shortUserInfo!!
    }

    fun loadSteemCurrency(listener: OnLoadingResultListener<CoinmarketcapCurrency>) {
        // TODO add checking expired time
        if (currencySteem.lastUpdateTime != 0L) {
            listener.onLoadingSuccess(currencySteem)
        } else {
            // TODO load new data and return result
        }
    }

    fun getSteemCurrency(): CoinmarketcapCurrency {
        return currencySteem
    }

    fun getSbdCurrency(): CoinmarketcapCurrency {
        return currencySBD
    }

    fun setSteemCurrency(value: CoinmarketcapCurrency) {
        currencySteem = value
    }

    fun loadSBDCurrency(listener: OnLoadingResultListener<CoinmarketcapCurrency>) {
        // TODO add checking expired time
        if (currencySBD.lastUpdateTime != 0L) {
            listener.onLoadingSuccess(currencySBD)
        } else {
            // TODO load new data and return result
        }
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

    }

    fun getTotalVestingData(): Array<Double> {
        return totalVestingData
    }

    fun setTotalVestingData(values: Array<Double>) {
        totalVestingData = values
    }

    interface OnLoadingResultListener<R> {
        fun onLoadingSuccess(result: R)

        fun onLoadingError(message: String)
    }

    fun clearAllData() {
        extUserData = null
        shortUserInfo = null
        SharedRepository().saveUserData(UserData(null, null, null, null))
        SharedRepository().saveBalanceData(Balance())
    }


}
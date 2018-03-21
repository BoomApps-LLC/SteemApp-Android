package com.boomapps.steemapp.main

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.boomapps.steemapp.BaseViewModel
import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.ViewState
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.SteemWorker
import com.boomapps.steemapp.repository.Storage
import com.boomapps.steemapp.repository.network.NetworkRepository

/**
 * Created by vgrechikha on 25.01.2018.
 */
class MainViewModel : BaseViewModel() {

    companion object {
        const val TAB_WALLET = 0
        const val TAB_EDIT = 1
        const val TAB_PROFILE = 2
    }


    var currentTab = TAB_WALLET

    var userData: MutableLiveData<UserData> = MutableLiveData()

    var balanceData: MutableLiveData<Balance> = MutableLiveData()


    fun getUserProfile(): MutableLiveData<UserData> {
        if (userData.value == null) {
            userData.value = Storage.get().getShortUserData()
            loadUserProfile()
        }
        return userData
    }

    fun signOut() {
        Storage.get().clearAllData()
        SteemWorker.get().signOut()

    }


    fun loadUserProfile() {
        if (userData.value?.nickname.isNullOrEmpty()) {
            userData.value = RepositoryProvider.instance.getSharedRepository().loadUserData()
        }
        if (!userData.value?.userName.isNullOrEmpty()) {
            state.value = ViewState.COMMON
            return
        }
        val nickName = userData.value?.nickname
        if (nickName.isNullOrEmpty()) {
            stringError = "can't find any login"
            state.value = ViewState.FAULT_RESULT
            return
        } else {
            state.value = ViewState.PROGRESS
            RepositoryProvider.instance.getNetworkRepository().loadExtendedUserProfile(nickName!!, object : NetworkRepository.OnRequestFinishCallback {

                override fun onSuccessRequestFinish() {
                    Log.d("MainViewModel", "onSuccessRequestFinish")
                    val exUserData = RepositoryProvider.instance.getNetworkRepository().extendedProfileResponse?.userExtended
                    if (exUserData != null) {
                        val newUserData = UserData(nickName, exUserData.profileMetadata.userName, exUserData.profileMetadata.photoUrl, userData.value?.postKey)
                        userData.value = newUserData
                        RepositoryProvider.instance.getSharedRepository().updateUserData(newUserData)
                    }
                    if (userData.value?.userName.isNullOrEmpty()) {
                        stringError = "UserExtended profile loading error."
                        state.value = ViewState.FAULT_RESULT
                    } else {
                        state.value = ViewState.SUCCESS_RESULT
                    }
                }

                override fun onFailureRequestFinish(throwable: Throwable) {
                    Log.d("MainViewModel", "doOnError")
                    stringError = throwable.localizedMessage
                    state.value = ViewState.FAULT_RESULT
                }
            })

        }

    }

    fun getBalance(): MutableLiveData<Balance> {
        if (balanceData.value == null) {
            balanceData.value = Balance()
            loadBalance()
        }
        return balanceData
    }

    fun loadBalance() {
        val newBalance = calculateBalance()
        if (newBalance != null) {
            balanceData.value = newBalance
            return
        }
        balanceData.value = RepositoryProvider.instance.getSharedRepository().loadBalance()
        val time: Long = balanceData.value!!.updateTime
        if (time > 0 || time - System.currentTimeMillis() > (1000 * 3600 * 24)) {
            return
        }
        state.value = ViewState.PROGRESS
        if (userData.value?.nickname.isNullOrEmpty()) {
            userData.value = RepositoryProvider.instance.getSharedRepository().loadUserData()
        }
        val nickName = userData.value?.nickname
        if (nickName.isNullOrEmpty()) {
            // nick is needed for data loading
            stringError = "can't find any login"
            state.value = ViewState.FAULT_RESULT
            return
        } else {
            RepositoryProvider.instance.getNetworkRepository().loadFullStartData(userData.value?.nickname!!, object : NetworkRepository.OnRequestFinishCallback {

                override fun onSuccessRequestFinish() {
                    val newBalance = calculateBalance()
                    if (newBalance != null) {
                        balanceData.value = newBalance
                    }
                    state.value = ViewState.SUCCESS_RESULT
                }

                override fun onFailureRequestFinish(throwable: Throwable) {

                    state.value = ViewState.FAULT_RESULT
                }
            })


        }
    }

    fun calculateBalance(): Balance? {
        val storage = Storage.get()
        val exData = storage.getUserExtendedData()
        val steemCurency = storage.getSteemCurrency()
        val sbdCurrency = storage.getSbdCurrency()
        val vestingShares = storage.getTotalVestingData()

        if (exData.id != 0L) {
            val sbdBalance = extractValue(exData.sbdBalance, "SBD")
            val sbdSavingBalance = extractValue(exData.sbdSavingBalance, "SBD")
            val steemBalance = extractValue(exData.steemBalance, "STEEM")
            val steemSavingBalance = extractValue(exData.steemSavingBalance, "STEEM")
            val vestsShare = extractValue(exData.vestShares, "VESTS")
            Log.d("MainViewModel", "calculateBalance:: combineFunction >> values[${steemBalance + steemSavingBalance}; ${sbdBalance + sbdSavingBalance}; ${vestsShare}]")
            if (vestingShares.size == 2 && vestingShares[0] != 0.0 && vestingShares[1] != 0.0) {
                val steemPower = vestingShares[0] * (vestsShare / (vestingShares[1]))
                Log.d("MainViewModel", "calculateBalance:: combineFunction >> ${vestsShare} VESTS = $steemPower STEEM")
                val price = (steemBalance + steemSavingBalance + steemPower) * steemCurency.usdPrice + (steemBalance + steemSavingBalance) * sbdCurrency.usdPrice
                Log.d("MainViewModel", "calculateBalance:: combineFunction >> fullPrice=$price")
                return Balance(
                        steemBalance,
                        steemSavingBalance,
                        sbdBalance,
                        sbdSavingBalance,
                        vestsShare,
                        price,
                        System.currentTimeMillis()
                )
            } else {
                return null
            }

        } else {
            return null
        }
    }

    fun extractValue(input: String?, currencyName: String): Double {
        if (input == null) {
            return 0.0
        }
        if (input.contains(currencyName)) {
            val subs = input.substringBefore(currencyName).trim()
            return subs.toDouble()
        } else {
            return input.toDouble()
        }

    }

}

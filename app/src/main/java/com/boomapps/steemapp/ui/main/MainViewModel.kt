/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.main

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.repository.currency.AmountRequestData
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.repository.entity.VoteState
import com.boomapps.steemapp.repository.entity.profile.ProfileResponse
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkResponseCode
import com.boomapps.steemapp.ui.BaseViewModel
import com.boomapps.steemapp.ui.ViewState
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Created by vgrechikha on 25.01.2018.
 */
class MainViewModel : BaseViewModel() {

    companion object {
        const val TAB_FEEDS = 0
        const val TAB_EDIT = 1
        const val TAB_PROFILE = 2
    }


    var currentTab = TAB_FEEDS

    var userData: MutableLiveData<UserData> = MutableLiveData()

    var balanceData: MutableLiveData<Balance> = MutableLiveData()

    var successfulPostingNumber: Int = -1

    var steemTradingAmount: MutableLiveData<OutputAmount> = MutableLiveData()


    fun getUserProfile(): MutableLiveData<UserData> {
        if (userData.value == null) {
            userData.value = RepositoryProvider.getPreferencesRepository().loadUserData()
            loadUserProfile()
        }
        return userData
    }

    fun signOut() {
        Single.fromCallable {
            RepositoryProvider.getDaoRepository().clearDB()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe()

        RepositoryProvider.getPreferencesRepository().clearAllData()
        RepositoryProvider.getSteemRepository().signOut()

    }


    private fun loadUserProfile() {
        if (userData.value?.nickname.isNullOrEmpty()) {
            userData.value = RepositoryProvider.getPreferencesRepository().loadUserData()
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
            RepositoryProvider.getNetworkRepository().loadExtendedUserProfile(nickName!!, object : NetworkRepository.OnRequestFinishCallback<ProfileResponse?> {

                override fun onSuccessRequestFinish(response: ProfileResponse?) {
                    Log.d("MainViewModel", "onSuccessRequestFinish")
                    val exUserData = response?.userExtended
                    if (exUserData != null) {
                        val newUserData = UserData(nickName, exUserData.profileMetadata.userName, exUserData.profileMetadata.photoUrl, userData.value?.postKey)
                        userData.value = newUserData
                        RepositoryProvider.getPreferencesRepository().updateUserData(newUserData)
                    }
                    if (userData.value?.userName.isNullOrEmpty()) {
                        stringError = "UserExtended profile loading error."
                        state.value = ViewState.FAULT_RESULT
                    } else {
                        state.value = ViewState.SUCCESS_RESULT
                    }
                }

                override fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable) {
                    Log.d("MainViewModel", "doOnError")
                    stringError = throwable.localizedMessage
                    state.value = ViewState.FAULT_RESULT
                }
            })

        }

    }

    fun getSteemUsdAmount(): MutableLiveData<OutputAmount> {
        if (steemTradingAmount.value == null) {
            steemTradingAmount.value = OutputAmount()
            loadSteemUsdAmount()
        }
        return steemTradingAmount
    }

    private fun loadSteemUsdAmount() {
        if (steemTradingAmount != null) {
            val inputAmount = steemTradingAmount.value?.inputAmount
            if (inputAmount != null && inputAmount > 0) {
                return
            }
        }
        RepositoryProvider.getNetworkRepository().loadOutputAmount(AmountRequestData(1.0f, "steem", "bitusd"), object : NetworkRepository.OnRequestFinishCallback<OutputAmount> {
            override fun onSuccessRequestFinish(response: OutputAmount) {
                steemTradingAmount.value = response
            }

            override fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable) {
                stringError = if (throwable.localizedMessage != null) {
                    throwable.localizedMessage
                } else {
                    throwable.message ?: "Cannot load currency"
                }
            }
        })
    }

    fun getBalance(): MutableLiveData<Balance> {
        if (balanceData.value == null) {
            balanceData.value = Balance()
            loadBalance()
        }
        return balanceData
    }

    private fun loadBalance() {
        balanceData.value = RepositoryProvider.getPreferencesRepository().loadBalance(false)
        val fullBalance = balanceData.value?.fullBalance
        if (fullBalance != null && fullBalance >= 0) {
            return
        }
        val time: Long = balanceData.value!!.updateTime
        if (time > 0 || time - System.currentTimeMillis() > (1000 * 3600)/* 1 hour */) {
            return
        }
        updateData()
    }

    @Volatile
    var isDataUpdating: Boolean = false

    fun updateData() {
        if (userData.value?.nickname.isNullOrEmpty()) {
            userData.value = RepositoryProvider.getPreferencesRepository().loadUserData()
        }
        val nick = userData.value?.nickname
        if (nick.isNullOrEmpty()) {
            return
        }
        if (isDataUpdating) {
            return
        }
        isDataUpdating = true
        RepositoryProvider.getNetworkRepository().loadFullStartData(nick!!, object : NetworkRepository.OnRequestFinishCallback<Any?> {

            override fun onSuccessRequestFinish(response: Any?) {
                userData.value = RepositoryProvider.getPreferencesRepository().loadUserData()
                balanceData.value = RepositoryProvider.getPreferencesRepository().loadBalance(true)
                state.value = ViewState.SUCCESS_RESULT
                isDataUpdating = false
            }

            override fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable) {
                isDataUpdating = false
                stringError = if (throwable.localizedMessage != null) {
                    throwable.localizedMessage
                } else {
                    throwable.message ?: "empty error"
                }

            }
        })
    }

    fun shouldShowVoteDialog(): Boolean {
        successfulPostingNumber = RepositoryProvider.getPreferencesRepository().loadSuccessfulPostingNumber()
        when (successfulPostingNumber) {
            1 -> return RepositoryProvider.getPreferencesRepository().loadVotingState() == VoteState.UNDEFINED
            3 -> return RepositoryProvider.getPreferencesRepository().loadVotingState() in arrayOf(VoteState.REJECTED, VoteState.UNDEFINED)
        }
        return false
    }

    fun updateVotingState(isRejected: Boolean) {
        RepositoryProvider.getPreferencesRepository().saveVotingState(
                if (isRejected) {
                    VoteState.REJECTED
                } else {
                    VoteState.VOTED
                }
        )
    }

}

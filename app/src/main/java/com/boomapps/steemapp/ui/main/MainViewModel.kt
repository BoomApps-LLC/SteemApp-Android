package com.boomapps.steemapp.ui.main

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.repository.entity.VoteState
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.ui.BaseViewModel
import com.boomapps.steemapp.ui.ViewState

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


    fun getUserProfile(): MutableLiveData<UserData> {
        if (userData.value == null) {
            userData.value = ServiceLocator.getPreferencesRepository().loadUserData()
            loadUserProfile()
        }
        return userData
    }

    fun signOut() {
        ServiceLocator.getPreferencesRepository().clearAllData()
        ServiceLocator.getSteemRepository().signOut()

    }


    private fun loadUserProfile() {
        if (userData.value?.nickname.isNullOrEmpty()) {
            userData.value = ServiceLocator.getPreferencesRepository().loadUserData()
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
            ServiceLocator.getNetworkRepository().loadExtendedUserProfile(nickName!!, object : NetworkRepository.OnRequestFinishCallback {

                override fun onSuccessRequestFinish() {
                    Log.d("MainViewModel", "onSuccessRequestFinish")
                    val exUserData = ServiceLocator.getNetworkRepository().extendedProfileResponse?.userExtended
                    if (exUserData != null) {
                        val newUserData = UserData(nickName, exUserData.profileMetadata.userName, exUserData.profileMetadata.photoUrl, userData.value?.postKey)
                        userData.value = newUserData
                        ServiceLocator.getPreferencesRepository().updateUserData(newUserData)
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

    private fun loadBalance() {
        balanceData.value = ServiceLocator.getPreferencesRepository().loadBalance(false)
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
            userData.value = ServiceLocator.getPreferencesRepository().loadUserData()
        }
        val nick = userData.value?.nickname
        if (nick.isNullOrEmpty()) {
            return
        }
        if (isDataUpdating) {
            return
        }
        isDataUpdating = true
        ServiceLocator.getNetworkRepository().loadFullStartData(nick!!, object : NetworkRepository.OnRequestFinishCallback {

            override fun onSuccessRequestFinish() {
                userData.value = ServiceLocator.getPreferencesRepository().loadUserData()
                balanceData.value = ServiceLocator.getPreferencesRepository().loadBalance(true)
                state.value = ViewState.SUCCESS_RESULT
                isDataUpdating = false
            }

            override fun onFailureRequestFinish(throwable: Throwable) {
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
        successfulPostingNumber = ServiceLocator.getPreferencesRepository().loadSuccessfulPostingNumber()
        when (successfulPostingNumber) {
            1 -> return ServiceLocator.getPreferencesRepository().loadVotingState() == VoteState.UNDEFINED
            3 -> return ServiceLocator.getPreferencesRepository().loadVotingState() in arrayOf(VoteState.REJECTED, VoteState.UNDEFINED)
        }
        return false
    }

    fun updateVotingState(isRejected: Boolean) {
        ServiceLocator.getPreferencesRepository().saveVotingState(
                if (isRejected) {
                    VoteState.REJECTED
                } else {
                    VoteState.VOTED
                }
        )
    }

}

package com.boomapps.steemapp.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.boomapps.steemapp.MDCOLOR_TYPE
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.UserData
import com.boomapps.steemapp.editor.tabs.CategoryItem
import com.boomapps.steemapp.getMatColor
import com.boomapps.steemapp.main.Balance
import com.boomapps.steemapp.utils.DeCryptor
import com.boomapps.steemapp.utils.EnCryptor
import com.boomapps.steemapp.utils.SettingsRepository

/**
 * Created by Vitali Grechikha on 28.01.2018.
 */
class SharedRepository {

    private val enCryptor = EnCryptor()
    private val deCryptor = DeCryptor()

    private fun getReadableSharedPreferences(): SharedPreferences {
        return SteemApplication.instance.getSharedPreferences("steem_shares", Context.MODE_PRIVATE)
    }

    private fun getSharedPreferencesEditor(): SharedPreferences.Editor {
        return getReadableSharedPreferences().edit()
    }

    private fun getStoryReadableSharedPreferences(): SharedPreferences {
        return SteemApplication.instance.getSharedPreferences("story_shares", Context.MODE_PRIVATE)
    }

    private fun getStorySharedPreferencesEditor(): SharedPreferences.Editor {
        return getStoryReadableSharedPreferences().edit()
    }


    fun saveUserData(userData: UserData) {
        val editor = getSharedPreferencesEditor()
        editor.apply {
            putString("nickname", userData.nickname)
            putString("username", userData.userName)
            putString("photo_url", userData.photoUrl)
        }.apply()
        if (userData.postKey != null) {
            val bytes = enCryptor.encryptText(userData.postKey)
            val encryptedPostingKey = Base64.encodeToString(bytes, Base64.DEFAULT)
            SettingsRepository.setProperty("posting_key", encryptedPostingKey, enCryptor.iv!!, SteemApplication.instance)
        } else {
            SettingsRepository.clearAllProperties(SteemApplication.instance)
        }
    }

    fun updateUserData(userData: UserData) {
        val old = loadUserData()
        var nickname = old.nickname
        var username = old.userName
        var photoUrl = old.photoUrl
        var postKey = old.postKey
        if (nickname.isNullOrEmpty()) {
            nickname = userData.nickname
        }
        if (username.isNullOrEmpty()) {
            username = userData.nickname
        }
        if (photoUrl.isNullOrEmpty()) {
            photoUrl = userData.photoUrl
        }
        if (postKey.isNullOrEmpty()) {
            postKey = userData.postKey
        }
        saveUserData(UserData(nickname, username, photoUrl, postKey))
    }

    fun loadUserData(): UserData {
        val prefs = getReadableSharedPreferences()
        val nick = prefs.getString("nickname", "")
        val username = prefs.getString("username", "")
        val photoUrl = prefs.getString("photo_url", "")
        val encryptedDataInfo = SettingsRepository.getProperty("posting_key", SteemApplication.instance)
        val postingKey = if (encryptedDataInfo.iv == null) {
            ""
        } else {
            deCryptor.decryptData(Base64.decode(encryptedDataInfo.data, Base64.DEFAULT), encryptedDataInfo.iv!!)
        }
        return UserData(nick, username, photoUrl, postingKey)
    }

    fun saveBalanceData(balance: Balance?) {
        if (balance != null) {
            val editor = getSharedPreferencesEditor()
            editor.apply {
                putFloat("steemBalance", balance.steemBalance.toFloat())
                putFloat("steemSavingBalance", balance.steemSavingBalance.toFloat())
                putFloat("sbdBalance", balance.sbdBalance.toFloat())
                putFloat("sbdSavingBalance", balance.sbdSavingBalance.toFloat())
                putFloat("vestShares", balance.vestShares.toFloat())
                putFloat("fullBalance", balance.fullBalance.toFloat())
                putLong("updateTime", balance.updateTime)

            }.apply()
        }
    }

    fun loadBalance(): Balance {
        val prefs = getReadableSharedPreferences()
        val sb = prefs.getFloat("steemBalance", 0f)
        val ssb = prefs.getFloat("steemSavingBalance", 0f)
        val sbdb = prefs.getFloat("sbdBalance", 0f)
        val sbdsb = prefs.getFloat("sbdSavingBalance", 0f)
        val vs = prefs.getFloat("vestShares", 0f)
        val fullBalance = prefs.getFloat("fullBalance", 0f)
        val updateTime = prefs.getLong("updateTime", 0L)
        return Balance(
                sb.toDouble(),
                ssb.toDouble(),
                sbdb.toDouble(),
                sbdsb.toDouble(),
                vs.toDouble(),
                fullBalance.toDouble(),
                updateTime)
    }

    fun saveStoryData(storyInstance: StoryInstance) {
        val editor = getStorySharedPreferencesEditor()
        val cSet: MutableSet<String> = mutableSetOf()
        for (i in storyInstance.categories.indices) {
            cSet.add(storyInstance.categories[i].stringValue)
        }
        editor.apply {
            putString("title", storyInstance.title)
            putString("story", storyInstance.story)
            putStringSet("categories", cSet)
        }.apply()
    }

    fun loadStoryData(): StoryInstance {
        val prefs = getStoryReadableSharedPreferences()
        val title = prefs.getString("title", "")
        val story = prefs.getString("story", "")
        val cSet: Set<String> = prefs.getStringSet("categories", setOf())
        val aList: ArrayList<CategoryItem> = arrayListOf()
        if (cSet.isNotEmpty()) {
            val context = SteemApplication.instance
            for (cat in cSet) {

                aList.add(CategoryItem(cat, context.resources.getMatColor(MDCOLOR_TYPE)))
            }
        }
        return StoryInstance(title, story, aList)
    }
}

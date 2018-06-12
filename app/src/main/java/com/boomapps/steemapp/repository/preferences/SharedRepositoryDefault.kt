package com.boomapps.steemapp.repository.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.boomapps.steemapp.MDCOLOR_TYPE
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.ui.editor.tabs.CategoryItem
import com.boomapps.steemapp.getMatColor
import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.repository.StoryInstance
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.VoteState
import com.boomapps.steemapp.repository.entity.profile.UserExtended
import com.boomapps.steemapp.utils.Crypto
import com.boomapps.steemapp.utils.SettingsRepository

/**
 * Created by Vitali Grechikha on 28.01.2018.
 */
class SharedRepositoryDefault : SharedRepository {

    companion object {
        @JvmStatic
        private val crypto = Crypto()
    }

    var storage: MemoryStorage = MemoryStorage()
    var balanceHelper: BalanceHelper = BalanceHelper(loadBalanceFromPrefs())


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


    override fun saveUserData(userData: UserData) {
        val editor = getSharedPreferencesEditor()
        editor.apply {
            putString("nickname", userData.nickname)
            putString("username", userData.userName)
            putString("photo_url", userData.photoUrl)
        }.apply()
        if (userData.postKey != null) {
            val bytes = crypto.encrypt(userData.postKey)
            val encryptedPostingKey = Base64.encodeToString(bytes, Base64.DEFAULT)
            SettingsRepository.setProperty(
                    "posting_key", encryptedPostingKey, crypto.getIvFromEncryptor(), SteemApplication.instance)
        } else {
            SettingsRepository.clearAllProperties(SteemApplication.instance)
        }
    }

    override fun updateUserData(userData: UserData) {
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

    override fun loadUserData(): UserData {
        if (!isUserSaved()) return UserData(null, null, null, null)
        val prefs = getReadableSharedPreferences()
        val nick = prefs.getString("nickname", "")
        val username = prefs.getString("username", "")
        val photoUrl = prefs.getString("photo_url", "")
        val encryptedDataInfo = SettingsRepository.getProperty("posting_key", SteemApplication.instance)
        crypto.setIvForDecryptor(encryptedDataInfo.iv)
        val postingKey = if (encryptedDataInfo.data == null) {
            ""
        } else {
            crypto.decrypt(Base64.decode(encryptedDataInfo.data, Base64.DEFAULT))
        }
        return UserData(nick, username, photoUrl, postingKey)
    }


    override fun updatePostingKey(newKey: String?) {
        val uData = loadUserData()
        val newUserData = UserData(uData.nickname, uData.userName, uData.photoUrl, newKey)
        saveUserData(newUserData)
    }

    private fun isUserSaved(): Boolean {
        return getReadableSharedPreferences().getString("nickname", "") != ""
    }

    override fun isUserLogged(): Boolean {
        return getReadableSharedPreferences().getString("nickname", "") != ""
    }

    override fun saveBalanceData(balance: Balance?) {
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

    override fun loadBalance(recalculate: Boolean): Balance {
        if (recalculate || !balanceHelper.hasBalance()) {
            val b = balanceHelper.calculateBalance(storage.getSteemCurrency(), storage.getSbdCurrency(), storage.getTotalVestingData())
            saveBalanceInPrefs(b)
            return b
        } else {
            return balanceHelper.balance
        }
    }

    private fun loadBalanceFromPrefs(): Balance {
        var result = Balance()
        try {
            val prefs = getReadableSharedPreferences()
            val sb = prefs.getFloat("steemBalance", 0f)
            val ssb = prefs.getFloat("steemSavingBalance", 0f)
            val sbdb = prefs.getFloat("sbdBalance", 0f)
            val sbdsb = prefs.getFloat("sbdSavingBalance", 0f)
            val vs = prefs.getFloat("vestShares", 0f)
            val fullBalance = prefs.getFloat("fullBalance", -1f)
            val updateTime = prefs.getLong("updateTime", 0L)
            result = Balance(
                    sb.toDouble(),
                    ssb.toDouble(),
                    sbdb.toDouble(),
                    sbdsb.toDouble(),
                    vs.toDouble(),
                    fullBalance.toDouble(),
                    updateTime)
        } catch (exc: ClassCastException) {

        } finally {
            return result
        }

    }

    private fun saveBalanceInPrefs(balance: Balance) {
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

    override fun saveStoryData(storyInstance: StoryInstance) {
        val editor = getStorySharedPreferencesEditor()
        val cSet: MutableSet<String> = mutableSetOf()
        for (i in storyInstance.categories.indices) {
            cSet.add(storyInstance.categories[i].stringValue)
        }
        editor.apply {
            putString("title", storyInstance.title)
//            putString("story", storyInstance.story)
            putStringSet("categories", cSet)
        }.apply()
    }

    override fun loadStoryData(): StoryInstance {
        val prefs = getStoryReadableSharedPreferences()
        val title = prefs.getString("title", "")
//        val story = prefs.getString("story", "")
        val cSet: Set<String> = prefs.getStringSet("categories", setOf())
        val aList: ArrayList<CategoryItem> = arrayListOf()
        if (cSet.isNotEmpty()) {
            val context = SteemApplication.instance
            for (cat in cSet) {

                aList.add(CategoryItem(cat, context.resources.getMatColor(MDCOLOR_TYPE)))
            }
        }
        return StoryInstance(title, "", aList)
    }

    override fun saveLastTimePosting(currentTimeMillis: Long) {
        val editor = getSharedPreferencesEditor()
        editor.putLong("last_posting_time", currentTimeMillis)
        editor.apply()
    }

    override fun loadLastTimePosting(): Long {
        val prefs = getReadableSharedPreferences()
        return prefs.getLong("last_posting_time", 0L)
    }

    override fun isFirstLaunch(): Boolean {
        val prefs = getReadableSharedPreferences()
        return prefs.getBoolean("first_launch", true)
    }

    override fun setFirstLaunchState(isFirst: Boolean) {
        val editor = getSharedPreferencesEditor()
        editor.putBoolean("first_launch", isFirst)
        editor.apply()
    }

    override fun saveSuccessfulPostingNumber(num: Int) {
        val editor = getSharedPreferencesEditor()
        editor.putInt("SuccessfulPostingNumber", num)
        editor.apply()
    }

    override fun loadSuccessfulPostingNumber(): Int {
        val prefs = getReadableSharedPreferences()
        return prefs.getInt("SuccessfulPostingNumber", 0)
    }

    override fun saveVotingState(state: VoteState) {
        val editor = getSharedPreferencesEditor()
        editor.putInt("voting_rejected", state.ordinal)
        editor.apply()
    }

    override fun loadVotingState(): VoteState {
        val prefs = getReadableSharedPreferences()
        return VoteState.values().get(prefs.getInt("voting_rejected", 0))
    }

    override fun saveSteemCurrency(currency: CoinmarketcapCurrency) {
        storage.setSteemCurrency(currency)
    }

    override fun saveSBDCurrency(currency: CoinmarketcapCurrency) {
        storage.setSBDCurrency(currency)
    }

    override fun saveUserExtendedData(data: UserExtended) {
        storage.setUserExtended(data)
        balanceHelper.updateUserValues(data)
    }

    override fun saveTotalVestingData(data: Array<Double>) {
        storage.setTotalVestingData(data)
    }

    override fun clearAllData() {
        storage.clearAllData()
        saveBalanceInPrefs(Balance())
        balanceHelper = BalanceHelper(Balance())
        saveUserData(UserData(null, null, null, null))
    }


}

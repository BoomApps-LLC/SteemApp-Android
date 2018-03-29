package com.boomapps.steemapp.repository.preferences

import com.boomapps.steemapp.repository.Balance
import com.boomapps.steemapp.repository.currency.CoinmarketcapCurrency
import com.boomapps.steemapp.repository.entity.profile.UserExtended

class BalanceHelper(var balance: Balance) {

    fun hasBalance(): Boolean {
        return balance.updateTime > 0L && balance.fullBalance >= 0.0
    }

    fun updateUserValues(exData: UserExtended) {
        val sbdBalance = extractValue(exData.sbdBalance, "SBD")
        val sbdSavingBalance = extractValue(exData.sbdSavingBalance, "SBD")
        val steemBalance = extractValue(exData.steemBalance, "STEEM")
        val steemSavingBalance = extractValue(exData.steemSavingBalance, "STEEM")
        val vestsShare = extractValue(exData.vestShares, "VESTS")
        balance = Balance(
                steemBalance,
                steemSavingBalance,
                sbdBalance,
                sbdSavingBalance,
                vestsShare,
                0.0,
                0
        )
    }


    fun calculateBalance(steemCurency: CoinmarketcapCurrency, sbdCurrency: CoinmarketcapCurrency, vestingShares: Array<Double>): Balance {
        if (steemCurency.currencyName.isEmpty()) {
            return Balance()
        }
        if (sbdCurrency.currencyName.isEmpty()) {
            return Balance()
        }
        if (vestingShares.size != 2 || (vestingShares[0] == 0.0 && vestingShares[1] == 0.0)) {
            return balance
        }

//            Log.d("MainViewModel", "calculateBalance:: combineFunction >> values[${steemBalance + steemSavingBalance}; ${sbdBalance + sbdSavingBalance}; ${vestsShare}]")
        val steemPower = vestingShares[0] * (balance.vestShares / (vestingShares[1]))
//                Log.d("MainViewModel", "calculateBalance:: combineFunction >> ${vestsShare} VESTS = $steemPower STEEM")
        val price = (balance.steemBalance + balance.steemSavingBalance + steemPower) * steemCurency.usdPrice + (balance.steemBalance + balance.steemSavingBalance) * sbdCurrency.usdPrice
//                Log.d("MainViewModel", "calculateBalance:: combineFunction >> fullPrice=$price")
        balance = Balance(
                balance.steemBalance,
                balance.steemSavingBalance,
                balance.sbdBalance,
                balance.sbdSavingBalance,
                balance.vestShares,
                price,
                System.currentTimeMillis()
        )
        return balance
    }

    private fun extractValue(input: String?, currencyName: String): Double {
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
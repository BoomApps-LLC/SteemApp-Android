package com.boomapps.steemapp.ui.currencies

import android.arch.lifecycle.MutableLiveData
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.currency.AmountRequestData
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkResponseCode
import com.boomapps.steemapp.ui.BaseViewModel

class CurrenciesViewModel : BaseViewModel() {

    val data: MutableLiveData<ArrayList<OutputAmount>> = MutableLiveData()

    fun getCurrencies(): MutableLiveData<ArrayList<OutputAmount>> {
        if (data.value == null) {
            data.value = arrayListOf()
            loadCurrencies()
        }
        return data
    }

    private fun loadCurrencies() {
        for (r in requestedCurrencies) {
            RepositoryProvider.getNetworkRepository().loadOutputAmount(r, callback)
        }
    }


    private val callback = object : NetworkRepository.OnRequestFinishCallback<OutputAmount> {
        override fun onSuccessRequestFinish(response: OutputAmount) {
            var newData: ArrayList<OutputAmount> = arrayListOf()
            val currentData = data.value
            if (currentData != null) {
                newData.addAll(currentData)
            }
            newData.add(response)
            data.value = newData
        }

        override fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable) {
            // todo process failure
        }
    }


    val requestedCurrencies = arrayOf(
            AmountRequestData(1.0f, "steem", "bitusd"),
            AmountRequestData(1.0f, "sbd", "bitusd"),
            AmountRequestData(1.0f, "bts", "bitusd"),
            AmountRequestData(1.0f, "btc", "bitusd"),
            AmountRequestData(1.0f, "eth", "bitusd")
    )

}
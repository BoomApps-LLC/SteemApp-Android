package com.boomapps.steemapp.ui.currencies

import androidx.lifecycle.MutableLiveData
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.currency.AmountRequestData
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkResponseCode
import com.boomapps.steemapp.ui.BaseViewModel
import timber.log.Timber

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

    RepositoryProvider.getNetworkRepository()
        .loadOutputAmounts(requestedCurrencies, amountsCallback)
//
//    for (r in requestedCurrencies) {
//      RepositoryProvider.getNetworkRepository()
//          .loadOutputAmount(r, callback)
//    }
  }

  private val amountsCallback = object : NetworkRepository.OnRequestFinishCallback<ArrayList<OutputAmount>> {

    override fun onSuccessRequestFinish(newData: ArrayList<OutputAmount>) {
      var steemToUsd = 1.0f
      var ethToSteem = 1.0f
      var sbdToSteem = 1.0f
      if (newData != null) {
        for (item in newData) {
          when (item.inputCoinType) {
            "steem" -> steemToUsd = item.outputAmount
            "eth" -> ethToSteem = item.outputAmount
            "sbd" -> sbdToSteem = item.outputAmount
          }
        }
      }
      val formattedData = arrayListOf<OutputAmount>()
      formattedData.addAll(newData.filter { it.inputCoinType == "steem" })
      formattedData.add(OutputAmount(1.0f, "sbd", sbdToSteem * steemToUsd, "bitusd"))
      formattedData.addAll(newData.filter { it.inputCoinType == "bts" })
      formattedData.addAll(newData.filter { it.inputCoinType == "btc" })
      formattedData.add(OutputAmount(1.0f, "eth", ethToSteem * steemToUsd, "bitusd"))
      data.value = formattedData
    }

    override fun onFailureRequestFinish(
      code: NetworkResponseCode,
      throwable: Throwable
    ) {
      Timber.e(throwable, "Currencies loading error")
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

    override fun onFailureRequestFinish(
      code: NetworkResponseCode,
      throwable: Throwable
    ) {
      // todo process failure
    }
  }

  val requestedCurrencies = arrayListOf(
      AmountRequestData(1.0f, "steem", "bitusd"),
//      AmountRequestData(1.0f, "sbd", "bitusd"), // through steem <> bitusd
      AmountRequestData(1.0f, "bts", "bitusd"), // +
      AmountRequestData(1.0f, "btc", "bitusd"), // +
//      AmountRequestData(1.0f, "eth", "bitusd"), // through steem <> bitusd
      AmountRequestData(1.0f, "eth", "steem"),
      AmountRequestData(1.0f, "sbd", "steem")
  )

}
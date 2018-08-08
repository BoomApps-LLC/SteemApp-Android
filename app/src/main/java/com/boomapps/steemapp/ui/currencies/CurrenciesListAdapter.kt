package com.boomapps.steemapp.ui.currencies

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.currency.OutputAmount

class CurrenciesListAdapter(val context: Context) : RecyclerView.Adapter<CurrencyViewHolder>() {

  val data: ArrayList<OutputAmount> = arrayListOf()

  fun setData(newData: ArrayList<OutputAmount>) {
    data.clear()
    data.addAll(newData)
    notifyDataSetChanged()
  }

  fun addCurrency(c: OutputAmount) {
    for (item in data) {
      if (item.inputCoinType == c.inputCoinType && item.outputCoinType == c.outputCoinType) {
        return
      }
    }
    data.add(c)
    notifyItemInserted(data.size - 1)
  }

  fun addCurrencies(ac: ArrayList<OutputAmount>) {
    for (c in ac) {
      var found = false
      for (item in data) {
        if (item.inputCoinType == c.inputCoinType && item.outputCoinType == c.outputCoinType) {
          found = true
          break
        }
      }
      if (!found) {
        data.add(c)
        notifyItemInserted(data.size - 1)
      }
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): CurrencyViewHolder {
    return CurrencyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_current_rate, parent, false))
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(
    holder: CurrencyViewHolder,
    position: Int
  ) {
    if (data.size <= position) {
      return
    }
    holder.currencyInput.text =
        Html.fromHtml("<b>".plus(data[position].inputCoinType.toUpperCase()).plus("</b>").plus(" 1"))
    holder.currencyOutput.text = Html.fromHtml(
        "<b>$</b> ".plus(
            if (data[position].outputAmount.toString().contains(".")) {
              String.format("%.4f", data[position].outputAmount)
            } else {
              data[position].outputAmount.toString()
            }
        )
    )
  }
}
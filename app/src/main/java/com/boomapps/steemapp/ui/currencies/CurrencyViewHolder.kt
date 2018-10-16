package com.boomapps.steemapp.ui.currencies

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.boomapps.steemapp.R

class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var currencyInput: TextView = itemView.findViewById(R.id.iCurrentRate_tvFrom)
    var currencyOutput: TextView = itemView.findViewById(R.id.iCurrentRate_tvTo)
}
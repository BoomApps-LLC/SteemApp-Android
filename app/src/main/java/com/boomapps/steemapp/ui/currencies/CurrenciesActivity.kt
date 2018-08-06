package com.boomapps.steemapp.ui.currencies

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.currency.OutputAmount
import com.boomapps.steemapp.ui.BaseActivity
import com.boomapps.steemapp.ui.ViewState
import kotlinx.android.synthetic.main.activity_current_rate.*

class CurrenciesActivity : BaseActivity() {


    lateinit var viewModel: CurrenciesViewModel
    lateinit var currenciesList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_rate)
        viewModel = ViewModelProviders.of(this).get(CurrenciesViewModel::class.java)
        viewModel.state.observe(this, object : Observer<ViewState> {
            override fun onChanged(t: ViewState?) {
                when (t) {
                    ViewState.COMMON -> {
                        dismissProgress()
                    }
                    ViewState.PROGRESS -> {
                        showProgress("Processing ...")
                    }
                }
            }
        })
        setPoweredText()
        setCurrenciesList()
        viewModel.getCurrencies()
                .observe(this, Observer<ArrayList<OutputAmount>> { data ->
                    if (data != null) {
                        (currenciesList.adapter as CurrenciesListAdapter).addCurrencies(data)
                    }
                })
    }

    override fun dismissProgress() {

    }

    override fun showProgress(message: String) {

    }

    fun setCurrenciesList() {
        currenciesList = aCurrentRate_rvList
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        currenciesList.layoutManager = manager
        currenciesList.adapter = CurrenciesListAdapter(this)

    }


    private fun setPoweredText() {
        val powered1 = getString(R.string.blocktrades_powered_1)
        val powered2 = getString(R.string.blocktrades_powered_2)
        val fullPoweredString = powered1.plus(" ")
                .plus(powered2)
        val clSpan = object : ClickableSpan() {
            override fun onClick(widget: View?) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://blocktrades.us")))
            }

            override fun updateDrawState(ds: TextPaint?) {
                super.updateDrawState(ds)
                ds?.color = ContextCompat.getColor(this@CurrenciesActivity, R.color.violet)
                ds?.isUnderlineText = false
                ds?.typeface = Typeface.DEFAULT_BOLD
            }
        }

        if (fullPoweredString.indexOf(powered2) > -1) {
            val spannableString = SpannableString(fullPoweredString)
            spannableString.setSpan(
                    clSpan,
                    fullPoweredString.indexOf(powered2),
                    fullPoweredString.indexOf(powered2) + powered2.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            aCurrentRate_tvPowered.text = spannableString
        } else {
            aCurrentRate_tvPowered.text = fullPoweredString
        }

    }

}

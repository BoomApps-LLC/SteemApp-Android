package com.boomapps.steemapp.ui.currencies

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
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
    val bar = supportActionBar
    if (bar != null) {
      bar.setDisplayHomeAsUpEnabled(true)
    }
    viewModel = ViewModelProviders.of(this)
        .get(CurrenciesViewModel::class.java)
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

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun dismissProgress() {

  }

  override fun showProgress(message: String) {

  }

  fun setCurrenciesList() {
    currenciesList = aCurrentRate_rvList
    val manager = LinearLayoutManager(this)
    manager.orientation = RecyclerView.VERTICAL
    currenciesList.layoutManager = manager
    currenciesList.itemAnimator = DefaultItemAnimator()
    currenciesList.addItemDecoration(object : ItemDecoration() {

    })
    currenciesList.adapter = CurrenciesListAdapter(this)

  }

  private fun setPoweredText() {
    val powered1 = getString(R.string.blocktrades_powered_1)
    val powered2 = getString(R.string.blocktrades_powered_2)
    val fullPoweredString = powered1.plus(" ")
        .plus(powered2)
    val clSpan = object : ClickableSpan() {

      override fun onClick(widget: View) {
        Toast.makeText(this@CurrenciesActivity, "CLICK ON SPAN", Toast.LENGTH_LONG)
            .show()
        this@CurrenciesActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://blocktrades.us")))
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
      aCurrentRate_tvPowered.setText(spannableString)
    } else {
      aCurrentRate_tvPowered.text = fullPoweredString
    }
    aCurrentRate_tvPowered.setOnClickListener {
      Toast.makeText(this@CurrenciesActivity, "CLICK ON SPAN", Toast.LENGTH_LONG)
          .show()
      this@CurrenciesActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://blocktrades.us")))
    }
  }

}

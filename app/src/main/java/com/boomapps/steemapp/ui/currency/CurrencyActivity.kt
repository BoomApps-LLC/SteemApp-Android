package com.boomapps.steemapp.ui.currency

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.boomapps.steemapp.R
import kotlinx.android.synthetic.main.activity_current_rate.aCurrentRate_tvPowered

class CurrencyActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_current_rate)
    setPoweredText()
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
        ds?.color = ContextCompat.getColor(this@CurrencyActivity, R.color.violet)
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

package com.boomapps.steemapp.ui.post

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.boomapps.steemapp.R


class PostArticlBodyHolder(view: View) : RecyclerView.ViewHolder(view) {

    var textView: TextView = view.findViewById(R.id.article_textView)

    private val headersScale: Array<Float> = arrayOf(1.5f, 1.3f, 1.1f)

    companion object {
        fun create(context: Context, parent: ViewGroup): PostArticlBodyHolder {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_article_body, parent, false)
            return PostArticlBodyHolder(view)
        }
    }

    fun bind(context: Context, title: String, body: String) {
        val spBuilder = SpannableStringBuilder()
        if (title.isNotEmpty()) {
            spBuilder.append(title, getHeaderSpan(title, headersScale[0]), Spanned.SPAN_PARAGRAPH)

        }
        spBuilder.append("\n\n")
        // split body
        val splitted = body.split(Regex.fromLiteral("\n"))
        var emptyStr = 0
        for (str in splitted) {
            val trimmed = str.trim()
            if (trimmed.isBlank()) {
                emptyStr++
                continue
            }
            if (emptyStr >= 2) {
                spBuilder.append("\n\n")
            }
            emptyStr = 0
            if (trimmed.startsWith("#")) {
                val start = trimmed.indexOf("#")
                val end = trimmed.indexOf(" ", start)
                val scale = headersScale[Math.max(Math.min(0, end - start - 1), 2)]
                val out = if (trimmed.endsWith("#")) {
                    trimmed.substring(end + 1, trimmed.indexOf("#", trimmed.length - 3)).trim()
                } else {
                    trimmed.substring(end + 1).trim()
                }
                spBuilder.append(out, getHeaderSpan(out, scale), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                continue
            }
            if (trimmed.startsWith("___")) {
                val start = textView.length()
                // The space is necessary, the block requires some content:
                spBuilder.append("\n", CustomHRSpan(-0x1000000, 5.0f, 2.0f), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                // TODO later
                continue
            }
            if (trimmed.startsWith("!")) {
                val startDescr = trimmed.indexOf("[")
                val endDescr = trimmed.indexOf("]", startDescr + 1)
                val linkStart = trimmed.indexOf("(", endDescr + 1)
                val linkEnd = trimmed.indexOf(")", linkStart + 1)
                spBuilder.append("\n", getImageSpan(context, trimmed.substring(linkStart, linkEnd)), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                continue
            }
            spBuilder.append(trimmed)
        }

        textView.setText(spBuilder, TextView.BufferType.SPANNABLE)
    }


    private fun getHeaderSpan(text: String, scale: Float): Any {
        val spannableStr = SpannableString(text)
        spannableStr.setSpan(RelativeSizeSpan(scale), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spannableStr.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return spannableStr
    }

    private fun getImageSpan(context: Context, url: String): Any {
        return ImageSpan(context, Uri.parse(url))
    }
}



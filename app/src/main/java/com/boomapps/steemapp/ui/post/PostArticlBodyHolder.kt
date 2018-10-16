package com.boomapps.steemapp.ui.post

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boomapps.steemapp.R
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration


class PostArticlBodyHolder(view: View) : RecyclerView.ViewHolder(view) {

    var titleView: TextView = view.findViewById(R.id.article_titleView)
    var textView: TextView = view.findViewById(R.id.article_textView)

    companion object {
        fun create(context: Context, parent: ViewGroup): PostArticlBodyHolder {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_article_body, parent, false)
            return PostArticlBodyHolder(view)
        }
    }


    fun bind(configuration: SpannableConfiguration, title: String, body: String) {
        titleView.text = title
        Markwon.setMarkdown(textView, configuration, body)
    }
}



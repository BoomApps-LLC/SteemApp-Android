package com.boomapps.steemapp.ui.post

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.db.entities.CommentEntity

class ArticleAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var title: String = ""
    var body: String = ""
    var comments: ArrayList<CommentEntity> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_article_body -> PostArticlBodyHolder.create(context, parent)
            R.layout.item_article_comment -> PostArticleCommentHolder.create(context, parent)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int {
        return if (body.isEmpty()) {
            0
        } else {
            comments.size + 1
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_article_body
        } else {
            R.layout.item_article_comment
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_article_body -> {
                (holder as PostArticlBodyHolder).bind(context, title, body)
            }
            R.layout.item_article_comment -> {
                (holder as PostArticleCommentHolder).bind(context, comments[position - 1])
            }
        }
    }

    fun resetComments(data: Array<CommentEntity>?) {
        if (data == null) {
            return
        }

        if (comments.size > 0) {
            val lastSize = comments.size
            comments.clear()
            notifyItemRangeRemoved(1, lastSize)
        }
        comments.addAll(data)
        notifyItemRangeInserted(1, comments.size)
    }

    fun updateBody(titleValue: String, bodyValue: String) {
        title = titleValue
        body = bodyValue
        notifyDataSetChanged()
    }


}
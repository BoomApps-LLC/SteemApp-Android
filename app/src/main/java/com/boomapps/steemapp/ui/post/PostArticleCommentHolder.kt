package com.boomapps.steemapp.ui.post

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.Utils
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber

class PostArticleCommentHolder(view: View) : RecyclerView.ViewHolder(view) {

    var avatarImg: ImageView = view.findViewById(R.id.itemComment_avatar)
    var authorName: TextView = view.findViewById(R.id.itemComment_tvAuthor)
    var lastTimeActivity: TextView = view.findViewById(R.id.itemComment_tvLastActivityTime)
    var authorReputation: TextView = view.findViewById(R.id.itemComment_tvReputation)
    var commentText: TextView = view.findViewById(R.id.itemComment_tvText)
    var moneyCount: TextView = view.findViewById(R.id.itemComment_tvFullPrice)
    var subCommentsNumber: TextView = view.findViewById(R.id.aPost_tvCommentNumber)
    var repliesNumber: TextView = view.findViewById(R.id.aPost_tvLinkNumber)


    companion object {
        fun create(context: Context, parent: ViewGroup): PostArticleCommentHolder {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_article_comment, parent, false)
            return PostArticleCommentHolder(view)
        }
    }

    fun bind(context: Context, data: CommentEntity) {
        Glide.with(context)
                .load("https://steemitimages.com/u/${data.author}/avatar")
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImg)
        Timber.d("COMMENT: bind ${data.level} :: ${data.order} :: ${data.created}")
        itemView.setPadding(8+ data.level * 80, 0, 8, 0)
        authorName.text = data.author
        // TODO author reputation
        lastTimeActivity.text = Utils.get().getFormattedCommentTime(context, data.created)
        commentText.text = data.body
        moneyCount.text = String.format("%.02f", data.price)
        subCommentsNumber.text = data.votesNum.toString()
        // TODO set replies number

    }

}
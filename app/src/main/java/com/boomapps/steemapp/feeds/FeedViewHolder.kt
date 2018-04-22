package com.boomapps.steemapp.feeds

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.boomapps.steemapp.R

class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val image: ImageView = itemView.findViewById(R.id.feedCard_ivMain)
    val title: TextView = itemView.findViewById(R.id.feedCard_tvTitle)
    val text: TextView = itemView.findViewById(R.id.feedCard_tvText)
    val author: TextView = itemView.findViewById(R.id.feedCard_tvAuthor)
    val avatar: ImageView = itemView.findViewById(R.id.feedCard_ivAuthorAvatar)
    val lastTime: TextView = itemView.findViewById(R.id.feedCard_tvLastActivityTime)
    val counter: TextView = itemView.findViewById(R.id.feedCard_tvCounter)
    val fullPrice: TextView = itemView.findViewById(R.id.feedCard_tvFullPrice)
    val commentsNumber: TextView = itemView.findViewById(R.id.feedCard_tvCommentNumber)
    val linksNumber: TextView = itemView.findViewById(R.id.feedCard_tvLinkNumber)
    val votesNumber: TextView = itemView.findViewById(R.id.feedCard_tvVotesNumber)
}
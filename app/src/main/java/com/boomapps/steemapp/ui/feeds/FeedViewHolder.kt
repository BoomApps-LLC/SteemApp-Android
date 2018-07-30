/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.feeds

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.util.*

class FeedViewHolder(itemView: View, callback: Callback) : RecyclerView.ViewHolder(itemView) {

    interface Callback {

        enum class Events {
            VOTE,
            MENU
        }

        fun onHolderClick(position: Int)

        fun onHolderActionEvent(event: Events, position: Int)

    }


    val image: ImageView = itemView.findViewById(R.id.feedCard_ivMain)
    val title: TextView = itemView.findViewById(R.id.feedCard_tvTitle)
    val text: TextView = itemView.findViewById(R.id.feedCard_tvText)
    val author: TextView = itemView.findViewById(R.id.feedCard_tvAuthor)
    val avatar: ImageView = itemView.findViewById(R.id.feedCard_ivAuthorAvatar)
    val lastTime: TextView = itemView.findViewById(R.id.feedCard_tvLastActivityTime)
    val reputation: TextView = itemView.findViewById(R.id.feedCard_tvReputation)
    val votesPriceLayout: LinearLayout = itemView.findViewById(R.id.feedCard_priceLayout)
    val votesPrice: TextView = itemView.findViewById(R.id.feedCard_tvVotesPrice)
    val commentsNumber: TextView = itemView.findViewById(R.id.feedCard_tvCommentNumber)
    val linksNumber: TextView = itemView.findViewById(R.id.feedCard_tvLinkNumber)
    val votesNumber: TextView = itemView.findViewById(R.id.feedCard_tvVotesNumber)

    private var data: StoryEntity? = null

    init {
        itemView.setOnClickListener {
            callback.onHolderClick(this@FeedViewHolder.layoutPosition)
        }
        votesPriceLayout.setOnClickListener {
            callback.onHolderActionEvent(Callback.Events.VOTE, this@FeedViewHolder.layoutPosition)
        }
    }


    fun bind(story: StoryEntity?) {
        this.data = story
        title.text = if (data?.title.isNullOrEmpty()) {
            ""
        } else {
            data?.title
        }

        author.text = if (data?.author.isNullOrEmpty()) {
            ""
        } else {
            data?.author
        }
        text.text = if (data?.shortText.isNullOrEmpty()) {
            ""
        } else {
            data?.shortText
        }
        linksNumber.text = data?.linksNum.toString()
        commentsNumber.text = data?.commentsNum.toString()
        reputation.text = data?.reputation.toString()
        votesNumber.text = data?.votesNum.toString()
        votesPrice.text = String.format("$ %.2f", data?.price ?: 0.0)
        if (data?.isVoted == true) {
            votesPriceLayout.setBackgroundResource(R.drawable.bg_feed_card_price_voted_selector)
            votesPrice.setTextColor(ContextCompat.getColorStateList(votesPrice.context, R.color.feed_card_price_unvoted_text_selector))
        } else {
            votesPriceLayout.setBackgroundResource(R.drawable.bg_feed_card_price_unvoted_selector)
            votesPrice.setTextColor(ContextCompat.getColorStateList(votesPrice.context, R.color.feed_card_price_voted_text_selector))
        }
        val created = Calendar.getInstance()
        created.timeInMillis = data?.created ?: 0L
        var sCreated = "unknown"
        if (created.timeInMillis > 0) {
            Timber.d("Created time for ${story?.permlink} = ${created.timeInMillis}")
            val curCal = currentDate()
            val yearsDelta = curCal.get(Calendar.YEAR) - created.get(Calendar.YEAR)
            val monthDelta = curCal.get(Calendar.MONTH) - created.get(Calendar.MONTH)
            val dayDelta = curCal.get(Calendar.DAY_OF_YEAR) - created.get(Calendar.DAY_OF_YEAR)
            val hoursDelta = curCal.get(Calendar.HOUR_OF_DAY) - created.get(Calendar.HOUR_OF_DAY)
            val minutesDelta = curCal.get(Calendar.MINUTE) - created.get(Calendar.MINUTE)
            Timber.d("Created time y=$yearsDelta; m=$monthDelta; d=$dayDelta; h=$hoursDelta")
            if (dayDelta in 2..29) {
                lastTime.text = formatDate(dayDelta, itemView.context.resources.getQuantityString(R.plurals.days, dayDelta))
            } else if (dayDelta >= 30 && monthDelta in 1..11) {
                lastTime.text = formatDate(monthDelta, itemView.context.resources.getQuantityString(R.plurals.months, monthDelta))
            } else if (yearsDelta > 0) {
                lastTime.text = formatDate(yearsDelta, itemView.context.resources.getQuantityString(R.plurals.years, yearsDelta))
            } else if (hoursDelta in 1..23) {
                lastTime.text = formatDate(hoursDelta, itemView.context.resources.getQuantityString(R.plurals.hours, hoursDelta))
            } else if(minutesDelta in 0..59){
                lastTime.text = formatDate(minutesDelta, itemView.context.resources.getQuantityString(R.plurals.minutes, minutesDelta))
            }else{
                lastTime.text = itemView.context.getString(R.string.feed_card_date_format_yesterday)
7            }
        }

        val imageUrl = data?.mainImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(itemView)
                    .load(imageUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.centerCropTransform())
                    .into(image)
        } else {
            Glide.with(itemView)
                    .load(R.drawable.img_logo)
                    .apply(RequestOptions.centerCropTransform())
                    .into(image)

        }
        val avatarUrl = data?.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(itemView)
                    .load(avatarUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatar)
        }

    }

    private fun formatDate(days: Int, pluralValue: String): String {
        return String.format(itemView.context.getString(R.string.feed_card_date_format_common), days, pluralValue)
    }

    companion object {
        fun create(parent: ViewGroup, callback: Callback): FeedViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.feed_card, parent, false)
            return FeedViewHolder(view, callback)
        }

        fun currentDate(): Calendar {
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            return c
        }
    }


}
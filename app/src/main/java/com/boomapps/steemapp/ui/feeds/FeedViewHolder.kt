package com.boomapps.steemapp.ui.feeds

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.util.*

class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val image: ImageView = itemView.findViewById(R.id.feedCard_ivMain)
    val title: TextView = itemView.findViewById(R.id.feedCard_tvTitle)
    val text: TextView = itemView.findViewById(R.id.feedCard_tvText)
    val author: TextView = itemView.findViewById(R.id.feedCard_tvAuthor)
    val avatar: ImageView = itemView.findViewById(R.id.feedCard_ivAuthorAvatar)
    val lastTime: TextView = itemView.findViewById(R.id.feedCard_tvLastActivityTime)
    val reputation: TextView = itemView.findViewById(R.id.feedCard_tvReputation)
    val fullPrice: TextView = itemView.findViewById(R.id.feedCard_tvFullPrice)
    val commentsNumber: TextView = itemView.findViewById(R.id.feedCard_tvCommentNumber)
    val linksNumber: TextView = itemView.findViewById(R.id.feedCard_tvLinkNumber)
    val votesNumber: TextView = itemView.findViewById(R.id.feedCard_tvVotesNumber)

    private var data: StoryEntity? = null

    init {
        itemView.setOnClickListener {
            //            feed?.imgUrl?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                itemView.context.startActivity(intent)
//            }
            // TODO process on item click
        }
    }


    fun bind(story: StoryEntity?) {
        this.data = story
        val imageUrl = data?.mainImageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(itemView)
                    .load(imageUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.centerCropTransform())
                    .into(image)
        }
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
        fullPrice.text = String.format("$ %.2f", data?.price ?: 0.0)
        val created = Calendar.getInstance()
        created.timeInMillis = data?.created ?: 0L
        var sCreated = "unknown"
        if (created.timeInMillis > 0) {
            val curCal = currentDate()
            var yearsDelta = curCal.get(Calendar.YEAR) - created.get(Calendar.YEAR)
            var monthDelta = curCal.get(Calendar.MONTH) - created.get(Calendar.MONTH)
            var dayDelta = curCal.get(Calendar.DAY_OF_YEAR) - created.get(Calendar.DAY_OF_YEAR)
            if (yearsDelta > 0) {
                lastTime.text = String.format("%d %s ago", yearsDelta, itemView.context.resources.getQuantityString(R.plurals.years, yearsDelta))
            } else if (monthDelta > 0) {
                lastTime.text = String.format("%d %s ago", monthDelta, itemView.context.resources.getQuantityString(R.plurals.months, monthDelta))
            } else if (dayDelta > 1) {
                lastTime.text = String.format("%d %s ago", dayDelta, itemView.context.resources.getQuantityString(R.plurals.days, dayDelta))
            } else {
                lastTime.text = "yesterday"
            }
        }


    }

    companion object {
        fun create(parent: ViewGroup): FeedViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.feed_card, parent, false)
            return FeedViewHolder(view)
        }

        fun currentDate(): Calendar {
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            return c
        }
    }


}
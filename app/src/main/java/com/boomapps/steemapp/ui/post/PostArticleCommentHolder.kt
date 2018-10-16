package com.boomapps.steemapp.ui.post

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.Utils
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration

class PostArticleCommentHolder(view: View) : RecyclerView.ViewHolder(view) {

    var id : Long = -1

    var avatarImg: ImageView = view.findViewById(R.id.itemComment_avatar)
    var authorName: TextView = view.findViewById(R.id.itemComment_tvAuthor)
    var lastTimeActivity: TextView = view.findViewById(R.id.itemComment_tvLastActivityTime)
    var authorReputation: TextView = view.findViewById(R.id.itemComment_tvReputation)
    var commentText: TextView = view.findViewById(R.id.itemComment_tvText)
    var moneyCount: TextView = view.findViewById(R.id.itemComment_tvFullPrice)

//    var repliesNumberIcon: ImageView = view.findViewById(R.id.itemComment_ivReplies)

    var moneyLayout : LinearLayout = view.findViewById(R.id.itemComment_votePriceLayout)
    var unvoteBackgroundColor = ContextCompat.getColorStateList(view.context, R.color.feed_card_price_unvoted_text_selector)
    var voteBackgroundColor = ContextCompat.getColorStateList(view.context, R.color.feed_card_price_voted_text_selector)

    var votesNumber: TextView = view.findViewById(R.id.itemComment_tvVotesNumber)
    var subCommentsNumber: TextView = view.findViewById(R.id.itemComment_tvCommentsNumber)
    var repliesNumber: TextView = view.findViewById(R.id.itemComment_tvRepliesNumber)

    companion object {
        fun create(context: Context, parent: ViewGroup): PostArticleCommentHolder {
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_article_comment_ll, parent, false)
            return PostArticleCommentHolder(view)
        }
    }

    fun bind(context: Context, configuration: SpannableConfiguration, data: CommentEntity) {
        if(id != data.commentId){
            Glide.with(context)
                    .load("https://steemitimages.com/u/${data.author}/avatar")
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatarImg)
        }
        id = data.commentId
//        Timber.d("COMMENT: bind ${data.level} :: ${data.order} :: ${data.created}")
        itemView.setPadding(8+ data.level * 80, 0, 8, 0)
        authorName.text = data.author
        // TODO author reputation
        lastTimeActivity.text = Utils.get().getFormattedCommentTime(context, data.created)
        Markwon.setMarkdown(commentText, configuration, data.body)
//        commentText.text = data.body
        moneyCount.text = String.format("%.02f", data.price)
        if (data.isVoted) {
            moneyLayout.setBackgroundResource(R.drawable.bg_feed_card_price_voted_selector)
            moneyCount.setTextColor(unvoteBackgroundColor)
        } else {
            moneyLayout.setBackgroundResource(R.drawable.bg_feed_card_price_unvoted_selector)
            moneyCount.setTextColor(voteBackgroundColor)
        }
        subCommentsNumber.text = data.childrenNum.toString()
        if(data.repliesNum == 0){
//            repliesNumberIcon.visibility = View.INVISIBLE
            repliesNumber.visibility = View.INVISIBLE
        }else{
            repliesNumber.text = data.repliesNum.toString()
//            repliesNumberIcon.visibility = View.VISIBLE
            repliesNumber.visibility = View.VISIBLE
        }
        votesNumber.text = data.votesNum.toString()

    }

}
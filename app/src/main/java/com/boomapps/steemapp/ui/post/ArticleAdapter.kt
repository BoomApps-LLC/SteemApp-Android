package com.boomapps.steemapp.ui.post

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import okhttp3.OkHttpClient
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.il.AsyncDrawableLoader
import ru.noties.markwon.il.ImageMediaDecoder
import ru.noties.markwon.il.NetworkSchemeHandler

class ArticleAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val imagesLoader = AsyncDrawableLoader.builder()
            .addSchemeHandler(NetworkSchemeHandler.create(OkHttpClient.Builder().build()))
            .addMediaDecoder(ImageMediaDecoder.create(context.resources))
            .build()

    private val articleSpannableConfiguration: SpannableConfiguration = SpannableConfiguration.builder(context)
            .imageSizeResolver(SteemImageSizeResolver())
            .htmlAllowNonClosedTags(true)
            .asyncDrawableLoader(imagesLoader)
            .build()

    private val commentSpannableConfiguration: SpannableConfiguration = SpannableConfiguration.builder(context)
            .htmlAllowNonClosedTags(true)
            .asyncDrawableLoader(imagesLoader)
            .build()

    var title: String = ""
    var body: String = ""
//    var comments: ArrayList<CommentEntity> = arrayListOf()

    var dataset: CommentsDataHolder = CommentsDataHolder(arrayListOf())




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_article_body -> PostArticlBodyHolder.create(context, parent)
            R.layout.item_article_comment_ll -> PostArticleCommentHolder.create(context, parent)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun getItemCount(): Int {
        return if (body.isEmpty()) {
            0
        } else {
            dataset.getCommentsNumber() + 1
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.item_article_body
        } else {
            R.layout.item_article_comment_ll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_article_body -> {
                (holder as PostArticlBodyHolder).bind(articleSpannableConfiguration, title, body)
            }
            R.layout.item_article_comment_ll -> {
                (holder as PostArticleCommentHolder).bind(context, commentSpannableConfiguration, dataset.getComment(position - 1))
            }
        }
    }

    fun resetComments(data: Array<CommentEntity>?) {
        if (data == null) {
            return
        }
        val oldSize = dataset.getCommentsNumber()
        if (oldSize > 0) {
            dataset.clearAllData()
            notifyItemRangeRemoved(1, oldSize)
        }
        dataset.setNewData(data)
        notifyItemRangeInserted(1, dataset.getCommentsNumber())
    }

    fun updateBody(titleValue: String, bodyValue: String) {
        title = titleValue
        body = bodyValue
        notifyDataSetChanged()
    }


}
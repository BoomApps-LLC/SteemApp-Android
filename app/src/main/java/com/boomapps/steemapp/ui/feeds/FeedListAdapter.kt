package com.boomapps.steemapp.ui.feeds

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber


class FeedListAdapter : RecyclerView.Adapter<FeedViewHolder>() {


    var dataset: ArrayList<FeedCardViewData> = arrayListOf()

    fun setData(data: ArrayList<FeedCardViewData>) {
        Timber.d("FeedListAdapter.setData({${data.size}})")
        dataset.clear()
        dataset.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: ArrayList<FeedCardViewData>) {
        val oldSize = dataset.size
        dataset.addAll(data)
        notifyItemRangeInserted(oldSize - 1, dataset.size - oldSize)
    }

    fun updateData(newData: ArrayList<FeedCardViewData>) {
        Timber.d("FeedListAdapter.updateData({${newData.size}})")
        if (dataset.size == 0) {
            dataset = newData
            notifyItemRangeInserted(0, dataset.size)
        } else {
            val diffResult = DiffUtil.calculateDiff(FeedDiffUtil(dataset, newData))
            dataset = newData
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        Timber.d("FeedListAdapter.CreateView()")
        return FeedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feed_card, parent, false))
    }

    override fun getItemCount(): Int {
        Timber.d("FeedListAdapter.getItemCount >> ${dataset.size}")
        return dataset.size
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        Timber.d("FeedListAdapter.onBindViewHolder($position) >> ${dataset[position]}")
        val data = dataset[position]
        if (data.imgUrl.isNotEmpty()) {
            Glide.with(holder.itemView)
                    .load(data.imgUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.image)
        }
        holder.title.text = if (data.title.isNotEmpty()) {
            data.title
        } else {
            "No Title"
        }
        holder.author.text = if (data.author.isNotEmpty()) {
            data.author
        } else {
            "No Author"
        }
        holder.text.text = if (data.text.isNotEmpty()) {
            data.text
        } else {
            "No Text"
        }
        holder.linksNumber.text = data.linksNum.toString()
        holder.commentsNumber.text = data.commentsNum.toString()
        holder.counter.text = data.fullCounter.toString()
        holder.votesNumber.text = data.votesNum.toString()
        holder.fullPrice.text = String.format("$ ")
    }
}
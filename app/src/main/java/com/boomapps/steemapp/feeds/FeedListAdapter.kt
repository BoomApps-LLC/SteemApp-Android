package com.boomapps.steemapp.feeds

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.bumptech.glide.Glide

class FeedListAdapter : RecyclerView.Adapter<FeedViewHolder>() {


    var dataset: ArrayList<FeedCardViewData> = arrayListOf()

    fun setData(data: ArrayList<FeedCardViewData>) {
        dataset.clear()
        dataset.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: ArrayList<FeedCardViewData>) {
        val oldSize = dataset.size
        dataset.addAll(data)
        notifyItemRangeInserted(oldSize - 1, dataset.size - oldSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(View.inflate(parent.context, R.layout.feed_card, null))
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        Glide.with(holder.itemView)
                .load(dataset[position].imgUrl)
                .into(holder.image)
        holder.title.text = dataset[position].title
        holder.author.text = dataset[position].author
    }
}
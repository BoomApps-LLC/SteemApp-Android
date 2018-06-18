/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.feeds

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.NetworkState
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import timber.log.Timber

class StoriesListAdapter(private val itemsCallback: FeedViewHolder.Callback, private val retryCallback: () -> Unit) : PagedListAdapter<StoryEntity, RecyclerView.ViewHolder>(STORY_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.feed_card -> FeedViewHolder.create(parent, itemsCallback)
            R.layout.feed_network_state -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.feed_card -> (holder as FeedViewHolder).bind(getItem(position))
            R.layout.feed_network_state -> (holder as NetworkStateItemViewHolder).bindTo(
                    networkState)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.feed_network_state
        } else {
            R.layout.feed_card
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        Timber.d("setNetworkState status=%s", networkState?.status)
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        val STORY_COMPARATOR = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                if (oldItem.commentsNum - newItem.commentsNum != 0 ||
                        oldItem.linksNum - newItem.linksNum != 0 ||
                        oldItem.commentsNum - newItem.commentsNum != 0 ||
                        oldItem.reputation - newItem.reputation != 0 ||
                        oldItem.price != newItem.price
                ) return false
                if (oldItem.avatarUrl != newItem.avatarUrl) return false
                if (oldItem.mainImageUrl != newItem.mainImageUrl) return false
                return true
            }

            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean =
                    oldItem.entityId == newItem.entityId

        }

    }
}
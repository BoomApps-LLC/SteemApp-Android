package com.boomapps.steemapp.ui.feeds

import android.arch.paging.PagedList
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.boomapps.steemapp.R

class FeedListHolder(val type: FeedType, val tabName: String, val feedView: View, val feedViewModel: FeedsViewModel, val callback: FeedListHolderCallback) {
    var refreshLayout: SwipeRefreshLayout = feedView.findViewById(R.id.refreshLayout)
    var recyclerView: RecyclerView = feedView.findViewById(R.id.recyclerView)
    lateinit var adapter: StoriesListAdapter

    init {
        val manager = LinearLayoutManager(feedView.context)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        initAdapter()

    }

    private fun initAdapter(){
        adapter = StoriesListAdapter{
            feedViewModel.retry(type)
        }
        recyclerView.adapter = adapter
        refreshLayout.setOnRefreshListener {
            callback.onRefresh(type)
        }
    }

    fun updateList(newData: PagedList<FeedCardViewData>) {
        refreshLayout.isRefreshing = false
        adapter.submitList(newData)
    }

    fun setProgressState(enabled: Boolean) {
        refreshLayout.isRefreshing = enabled
    }

}
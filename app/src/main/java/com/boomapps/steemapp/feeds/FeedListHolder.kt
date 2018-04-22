package com.boomapps.steemapp.feeds

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.boomapps.steemapp.R

class FeedListHolder(val tabName : String, val feedView: View) {
    var refreshLayout : SwipeRefreshLayout = feedView.findViewById(R.id.refreshLayout)
    var recyclerView : RecyclerView = feedView.findViewById(R.id.recyclerView)
    var adapter: FeedListAdapter

    init {
        val manager = LinearLayoutManager(feedView.context)
        manager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = manager
        adapter = FeedListAdapter()
        recyclerView.adapter = adapter
        refreshLayout.setOnRefreshListener {
            // TODO process on refresh
        }
    }

}
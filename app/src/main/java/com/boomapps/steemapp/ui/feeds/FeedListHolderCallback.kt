package com.boomapps.steemapp.ui.feeds

interface FeedListHolderCallback {

    fun onRefresh(type : FeedType)

    fun onItemClick(type : FeedType, position : Int)

    fun onActionClick(type : FeedType, position : Int, actions: Actions)

}
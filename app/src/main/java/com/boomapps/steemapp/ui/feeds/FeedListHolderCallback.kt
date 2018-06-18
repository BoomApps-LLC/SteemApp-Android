/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.feeds

import com.boomapps.steemapp.repository.FeedType

interface FeedListHolderCallback {

    fun onRefresh(type : FeedType)

    fun onItemClick(type : FeedType, position : Int)

    fun onActionClick(type : FeedType, position : Int, actions: Actions)

}
package com.boomapps.steemapp.repository.db

import android.arch.lifecycle.LiveData
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.Listing
import com.boomapps.steemapp.repository.NetworkState
import com.boomapps.steemapp.repository.db.entities.StoryEntity

interface DaoRepository {

    fun insertBlogEntities(blogEntities: ArrayList<StoryEntity>)


    fun insertFeedEntities(blogEntities: ArrayList<StoryEntity>)


    fun refresh(type: FeedType): LiveData<NetworkState>


    fun storiesFor(type: FeedType, pageSize: Int): Listing<StoryEntity>

}
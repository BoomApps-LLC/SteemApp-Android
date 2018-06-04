package com.boomapps.steemapp.repository.db

import android.arch.lifecycle.LiveData
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.Listing
import com.boomapps.steemapp.repository.NetworkState
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity

interface DaoRepository {


    fun refresh(type: FeedType): LiveData<NetworkState>


    fun storiesFor(type: FeedType, pageSize: Int): Listing<StoryEntity>


    fun getStory(storyId: Long): LiveData<StoryEntity>


    fun getStorySync(storyId: Long): StoryEntity


    fun getPost(postId: Long): PostEntity

    fun getPostLiveData(postId : Long) : LiveData<PostEntity>

    fun updatePost(postEntity: PostEntity)

    fun insertPost(postEntity: PostEntity)

    fun deletePost(postId: Long)

    fun deletePost(postEntity: PostEntity)

}
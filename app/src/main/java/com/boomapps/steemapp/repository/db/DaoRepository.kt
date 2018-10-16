/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db

import androidx.lifecycle.LiveData
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.Listing
import com.boomapps.steemapp.repository.NetworkState
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity

interface DaoRepository {


    fun refresh(type: FeedType): LiveData<NetworkState>

    fun updateStorySync(story: StoryEntity)

    fun storiesFor(type: FeedType, pageSize: Int): Listing<StoryEntity>


    fun getStory(storyId: Long): LiveData<StoryEntity>


    fun getStorySync(storyId: Long): StoryEntity


    fun getPost(postId: Long): PostEntity

    fun getPostLiveData(postId: Long): LiveData<PostEntity>

    fun getCommentsLiveData(postId: Long): LiveData<Array<CommentEntity>>

    fun insertComments(data : Array<CommentEntity>)

    fun updatePost(postEntity: PostEntity)

    fun insertPost(postEntity: PostEntity)

    fun deletePost(postId: Long)

    fun deletePost(postEntity: PostEntity)

    fun clearDB()


}
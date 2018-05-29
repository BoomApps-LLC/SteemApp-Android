package com.boomapps.steemapp.repository.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.boomapps.steemapp.repository.db.entities.PostEntity

@Dao
interface PostsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(postEntity: PostEntity)


    @Update
    fun updatePost(postEntity: PostEntity)


    @Delete
    fun deletePost(postEntity: PostEntity)

    @Query("DELETE FROM post_entities WHERE post_entity_id=:postId")
    fun deletePost(postId: Long)


    @Query("SELECT * FROM post_entities WHERE post_entity_id=:id")
    fun loadPostEntity(id: Long): PostEntity

    @Query("SELECT * FROM post_entities WHERE post_entity_id=:id")
    fun loadPostLiveData(id: Long): LiveData<PostEntity>


}
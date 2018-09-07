package com.boomapps.steemapp.repository.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import com.boomapps.steemapp.repository.db.entities.CommentEntity

@Dao
interface CommentsDao {

  @Query("SELECT * FROM comments WHERE root_id=:storyId ORDER BY level, time_last_update ASC")
  fun loadAllCommentsForStory(storyId : Long) : LiveData<CommentEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertComment(entity : CommentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertComments(entitie: Array<CommentEntity>)


  @Update
  fun updateComment(entity : CommentEntity)

  @Update
  fun updateComments(entities: Array<CommentEntity>)

  @Delete
  fun deleteComment(entity : CommentEntity)

  @Delete
  fun deleteComments(entities: Array<CommentEntity>)

}
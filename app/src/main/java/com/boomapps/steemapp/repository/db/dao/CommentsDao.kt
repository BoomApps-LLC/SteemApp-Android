package com.boomapps.steemapp.repository.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.boomapps.steemapp.repository.db.entities.CommentEntity

@Dao
interface CommentsDao {

  @Query("SELECT * FROM comments WHERE root_id=:storyId ORDER BY \"order\" ASC")
  fun loadAllCommentsForStory(storyId : Long) : LiveData<Array<CommentEntity>>

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
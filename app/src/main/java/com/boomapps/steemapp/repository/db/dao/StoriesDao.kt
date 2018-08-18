/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import com.boomapps.steemapp.repository.db.entities.StoryEntity

@Dao
interface StoriesDao {

//    FULL STORIES

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStory(storyEntity: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStories(storyEntities: Array<StoryEntity>)

    @Update
    fun updateStory(storyEntity: StoryEntity)

    @Update
    fun updateStories(storyEntities: Array<StoryEntity>)

    @Delete
    fun deleteStory(storyEntity: StoryEntity)

    @Delete
    fun deleteStories(storyEntities: Array<StoryEntity>)


    @Query("SELECT * FROM story_entities WHERE story_type=:type ORDER BY index_in DESC")
    fun loadAllStoriesRevertOrder(type: Int): DataSource.Factory<Int, StoryEntity>

    @Query("SELECT * FROM story_entities WHERE story_type=:type ORDER BY index_in ASC")
    fun loadAllStoriesDefaultOrder(type: Int): DataSource.Factory<Int, StoryEntity>

    @Query("SELECT * FROM story_entities WHERE story_type=:type AND permlink IN (:permlinks)")
    fun loadEntitiesForPage(type: Int, permlinks: Array<String>): LiveData<Array<StoryEntity>>

    @Query("SELECT MAX(index_in) + 1 FROM story_entities WHERE story_type = :type")
    fun getNextIndexInStories(type: Int): Int


    @Query("DELETE FROM story_entities WHERE story_type = :type")
    fun deleteStoriesFor(type: Int)

    @Query("SELECT * FROM story_entities WHERE story_entity_id = :storyId")
    fun loadStory(storyId: Long): LiveData<StoryEntity>

    @Query("SELECT * FROM story_entities WHERE story_entity_id = :storyId")
    fun loadStorySync(storyId: Long): StoryEntity

    @Query("DELETE FROM story_entities")
    fun deleteAllStories()



}
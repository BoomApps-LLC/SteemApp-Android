package com.boomapps.steemapp.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.boomapps.steemapp.repository.db.entities.UserEntity

@Dao
interface UserDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertUser(entity : UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertUsers(entitie: Array<UserEntity>)


  @Update
  fun updateUser(entity : UserEntity)

  @Update
  fun updateUsers(entities: Array<UserEntity>)

  @Delete
  fun deleteUser(entity : UserEntity)

  @Delete
  fun deleteUsers(entities: Array<UserEntity>)

}
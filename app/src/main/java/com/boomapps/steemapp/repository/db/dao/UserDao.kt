package com.boomapps.steemapp.repository.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update
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
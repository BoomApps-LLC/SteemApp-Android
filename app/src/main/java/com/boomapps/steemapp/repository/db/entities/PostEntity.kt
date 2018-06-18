/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "post_entities", indices = [(Index(value = ["url"], unique = true))])
class PostEntity {


    @PrimaryKey
    @ColumnInfo(name = "post_entity_id")
    var entityId: Long = 0

    var url: String = ""

    var body: String = ""

    var title : String = ""

}
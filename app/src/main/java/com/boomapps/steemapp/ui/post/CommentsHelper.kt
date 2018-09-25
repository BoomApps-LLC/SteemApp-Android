package com.boomapps.steemapp.ui.post

import com.boomapps.steemapp.repository.db.entities.CommentEntity

class CommentsHelper() {
    private val source : ArrayList<CommentEntity> = arrayListOf()
    private var levelNum : Int = 0
    private val tOther : ArrayList<CommentEntity> = arrayListOf()
    private val tLevel : ArrayList<CommentEntity> = arrayListOf()
    private val ordered : ArrayList<CommentEntity> = arrayListOf()

    fun setSource(newData : ArrayList<CommentEntity>){
        source.clear()
        source.addAll(newData)
    }

    fun getAll() : Array<CommentEntity>{
        return ordered.toTypedArray()
    }

    fun toTree(){
        // sort comments
        tLevel.clear()
        tOther.clear()
        levelNum = 0
        for (entity in source) {
            if (entity.rootId == entity.parentId) {
                tLevel.add(entity)
            }else{
                tOther.add(entity)
            }
        }
        tLevel.sortBy {
            it.order
        }
    }


}
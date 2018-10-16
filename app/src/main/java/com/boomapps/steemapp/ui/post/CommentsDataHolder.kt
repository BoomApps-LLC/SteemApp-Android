package com.boomapps.steemapp.ui.post

import com.boomapps.steemapp.repository.db.entities.CommentEntity
import timber.log.Timber

class CommentsDataHolder(var data: ArrayList<CommentEntity>) {

    val lvl0: ArrayList<CommentEntity> = arrayListOf()
    val childLvls: ArrayList<HashMap<Long, ArrayList<CommentEntity>>> = arrayListOf()
    val plantData: ArrayList<CommentEntity> = arrayListOf()

    init {
        recalculate()
    }


    fun setNewData(newData: Array<CommentEntity>) {
        data.addAll(newData)
        recalculate()
    }

    private fun recalculate() {
        // TODO move all calculations in other thread; maybe will be better to use Rx
        if (childLvls.isNotEmpty()) {
            childLvls?.clear()
        }
        if(lvl0.isNotEmpty()){
            lvl0.clear()
        }
        for (comment in data) {
            when (comment.level) {
                0 -> lvl0.add(comment)
                else -> addOnChildLvl(comment)
            }
        }
        lvl0.sortBy { it.order }
        plantData.addAll(getChildrenSorted(lvl0))
    }

    private fun addOnChildLvl(comment: CommentEntity) {
        val lvl = comment.level
        if (childLvls.size <= lvl) {
            while(childLvls.size != lvl + 1){
                childLvls.add(hashMapOf())
            }
        }
        if (childLvls[lvl-1][comment.parentId] == null) {
            childLvls[lvl-1][comment.parentId] = arrayListOf(comment)
        } else {
            childLvls[lvl-1][comment.parentId]?.add(comment)
        }
    }


    private fun getChildrenSorted(inData: ArrayList<CommentEntity>?): ArrayList<CommentEntity> {
        val result: ArrayList<CommentEntity> = arrayListOf()
        if (inData == null) {
            return result
        }
        inData.sortBy { it.order }
        for (comment in inData) {
            result.add(comment)
            if (comment.childrenNum > 0) {
                val lvl = comment.level
                result.addAll(getChildrenSorted(childLvls[lvl][comment.commentId]))
            }
        }
        return result
    }

    fun getComment(position: Int): CommentEntity {
        return plantData[position]
    }

    fun getCommentsNumber() : Int{
        Timber.d("getCommentsNumber >> ${plantData.size}")
        return plantData.size
    }

    fun clearAllData(){
        plantData.clear()
        data.clear()
        lvl0.clear()
        childLvls.clear()
    }

}
package com.boomapps.steemapp.ui.feeds

import android.support.v7.util.DiffUtil

class FeedDiffUtil(
        var oldData: ArrayList<FeedCardViewData>,
        var newData: ArrayList<FeedCardViewData>

) : DiffUtil.Callback() {


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition].equals(newData[newItemPosition])
    }
}
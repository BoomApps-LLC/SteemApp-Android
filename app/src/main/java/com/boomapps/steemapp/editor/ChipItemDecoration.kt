package com.boomapps.steemapp.editor

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by Vitali Grechikha on 24.01.2018.
 */
class ChipItemDecoration : RecyclerView.ItemDecoration(){

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        outRect?.set(12,16,12,0)
    }
}
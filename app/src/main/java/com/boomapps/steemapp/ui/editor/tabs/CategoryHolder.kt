/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.editor.tabs

import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.TouchDelegate
import android.view.View
import android.widget.TextView
import com.boomapps.steemapp.R

/**
 * Created by vgrechikha on 22.02.2018.
 */


class CategoryHolder(view: View, val onRemoveClickListener: OnRemoveClickListener) : RecyclerView.ViewHolder(view) {

    interface OnRemoveClickListener {
        fun onClick(position: Int)
    }


    val removeControl: View;
    val valueControl: TextView

    init {
        removeControl = view.findViewById(R.id.itemCategoryRemove)
        valueControl = view.findViewById(R.id.itemCategoryTextValue)
        // next code is used for extending touch area of remove tag control
        // @link{https://developer.android.com/training/gestures/viewgroup.html}
        view.post {
            val delegateArea = Rect()
            removeControl.getHitRect(delegateArea)
            delegateArea.top += 16
            delegateArea.bottom += 16
            delegateArea.left += 16
            delegateArea.right += 16
            view.touchDelegate = TouchDelegate(delegateArea, removeControl)
        }

    }

    fun setValue(value: CategoryItem, clickListener: OnRemoveClickListener) {
        valueControl.text = value.stringValue
        removeControl.setOnClickListener({
            onRemoveClickListener.onClick(layoutPosition)
        })
        val gd: GradientDrawable = itemView.getBackground() as GradientDrawable
        //To shange the solid color
        gd.setColor(value.color)
    }

}
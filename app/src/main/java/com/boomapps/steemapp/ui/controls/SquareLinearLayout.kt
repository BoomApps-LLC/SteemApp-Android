/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.controls

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * Created by Vitali Grechikha on 28.01.2018.
 */
class SquareLinearLayout : LinearLayout {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec > heightMeasureSpec)
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        else
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
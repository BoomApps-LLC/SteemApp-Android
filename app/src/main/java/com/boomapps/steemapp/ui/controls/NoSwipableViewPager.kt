/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.controls

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by Vitali Grechikha on 22.01.2018.
 */
class NoSwipableViewPager : ViewPager {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // Never allow swiping to switch between pages
        return false
    }
}
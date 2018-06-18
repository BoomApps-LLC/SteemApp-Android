/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.editor

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

/**
 * Created by Vitali Grechikha on 23.01.2018.
 */
class CustomEditText : EditText {
    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event?.getAction() == KeyEvent.ACTION_UP) {
            if(text.length == 2){
                return false;
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }
}
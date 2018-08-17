/*
 * Copyright 2018, BoomApps LLC.
 * All rights reserved.
*/
package jp.wasabeef.richeditor

import android.webkit.JavascriptInterface

/**
 * Created by Anatole Salanevich on 21.07.2018.
 */
class WebAppInterface {

    var onTextClickListener: RichEditor.OnTextClickListener? = null

    @JavascriptInterface
    fun onTextClick(position: Int) {
        onTextClickListener?.onTextClick(position)
    }

    @JavascriptInterface
    fun onTextSelect(begin: Int, end: Int) {
        onTextClickListener?.onTextSelect(begin, end)
    }

}
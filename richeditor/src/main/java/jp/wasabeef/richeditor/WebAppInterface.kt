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

}
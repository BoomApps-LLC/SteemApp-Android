package com.boomapps.steemapp.ui.editor

import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vitali Grechikha on 20.01.2018.
 */
class EditorTabsAdapter constructor(private val layoutInflater: LayoutInflater, private val viewIds: IntArray, val onViewDestroyListener: OnViewDestroyListener) : PagerAdapter() {

    interface OnViewDestroyListener{

        fun onDestroy(position: Int)

    }

    val views: ArrayList<View?> = arrayListOf(null, null, null, null)

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(viewIds[position], container, false)
        views[position] = view
        container.addView(view)
                Log.d("EditorTabsAdapter", "instantiateItem($position)")
        return view
    }

    override fun getCount(): Int {
//        Log.d("EditorTabsAdapter", "getCount(${viewIds.size})")
        return viewIds.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
        views[position] = null
        onViewDestroyListener.onDestroy(position)
        Log.d("EditorTabsAdapter", "destroyItem(${position})")
    }

    fun getViewAtPosition(pos: Int): View? {
        if (pos >= views.size) {
            return null
        }
        return views[pos]
    }
}
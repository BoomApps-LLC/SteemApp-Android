package com.boomapps.steemapp.ui.help

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Vitali Grechikha on 20.01.2018.
 */
class HelpTabsAdapter constructor(private val layoutInflater: LayoutInflater, private val viewIds: IntArray) : PagerAdapter() {

    lateinit var views: Array<View?>

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        views = arrayOfNulls<View>(viewIds.size)
        val view = layoutInflater.inflate(viewIds[position], container, false)
        views[position] = view
        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return viewIds.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
        views[position] = null
    }

    fun getViewAtPosition(pos: Int): View? {
        if (pos >= views.size) {
            return null
        }
        return views[pos]
    }
}
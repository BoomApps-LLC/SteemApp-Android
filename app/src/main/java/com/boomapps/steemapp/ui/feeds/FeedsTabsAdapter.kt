package com.boomapps.steemapp.ui.feeds

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

class FeedsTabsAdapter(private val holders: Array<FeedListHolder>) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(holders[position].feedView)
        return holders[position]
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == (`object` as FeedListHolder).feedView
    }

    override fun getCount(): Int {
        return holders.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(holders[position].feedView)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return holders[position].tabName
    }

    fun getItem(position: Int) : FeedListHolder {
        return holders[position]
    }
}
package com.boomapps.steemapp.ui.feeds


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.PagerTitleStrip
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.FeedType

/**
 * A simple [Fragment] subclass.
 * Use the [FeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FeedsFragment : Fragment(), FeedListHolderCallback {

    lateinit var feedsPager: ViewPager
    lateinit var feedsPagerStrip: PagerTitleStrip

    lateinit var viewModel: FeedsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FeedsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_feeds, container, false)
        feedsPager = view.findViewById(R.id.feedsPager)
        feedsPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                updatePage(position)
            }
        })
        val adapter = FeedsTabsAdapter(arrayOf(
                FeedListHolder(FeedType.BLOG, "BLOG", View.inflate(context, R.layout.feed_list_view, null), viewModel, this),
                FeedListHolder(FeedType.FEED, "FEED", View.inflate(context, R.layout.feed_list_view, null), viewModel, this)
        ))
        feedsPager.adapter = adapter
        view.findViewById<TabLayout>(R.id.feedsTabs).setupWithViewPager(feedsPager)
        updatePage(0)
        return view
    }


    private fun updatePage(position: Int) {
        when (position) {
            0 ->
                viewModel.showList(FeedType.BLOG)
            1 ->
                viewModel.showList(FeedType.FEED)
        }

    }


    // Callback methods for interaction with lists
    override fun onRefresh(type: FeedType) {
        viewModel.refresh(type)
    }

    override fun onItemClick(type: FeedType, position: Int) {

    }

    override fun onActionClick(type: FeedType, position: Int, actions: Actions) {

    }

    companion object {
        @JvmStatic
        fun newInstance() =
                FeedsFragment()

        const val KEY_FEED_TYPE = "feed_type"

    }
}

package com.boomapps.steemapp.ui.feeds


import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.boomapps.steemapp.R
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.ui.post.PostViewActivity
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [FeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FeedsFragment : Fragment(), FeedListHolderCallback {

    lateinit var feedsPager: ViewPager

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

        val adapter = FeedsTabsAdapter(arrayOf(
                FeedListHolder(FeedType.BLOG, "BLOG", View.inflate(context, R.layout.feed_list_view, null), viewModel, this),
                FeedListHolder(FeedType.FEED, "FEED", View.inflate(context, R.layout.feed_list_view, null), viewModel, this)
        ))
        feedsPager.adapter = adapter
        view.findViewById<TabLayout>(R.id.feedsTabs).setupWithViewPager(feedsPager)
        feedsPager.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                // set page change listener
                addPagerListener()
                // call update page with delay
                // it need for opening screen without freezes
                Handler().postDelayed({ updatePage(0) }, 1000)
            }
        })
        return view
    }

    private fun addPagerListener() {
        feedsPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                Timber.d("onPageScrollStateChanged $state")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Timber.d("onPageScrolled $position - $positionOffset - $positionOffsetPixels")
                if (positionOffset == 0.0f) {
                    updatePage(position)
                }
            }

            override fun onPageSelected(position: Int) {
                Timber.d("onPageSelected $position")
//                updatePage(position)
            }
        })
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
        val story = viewModel.getStory(type, position)
        if (story != null && story.url.isNotEmpty()) {
            val postIntent = Intent(this.context, PostViewActivity::class.java)
            postIntent.putExtra(PostViewActivity.EXTRA_URL, story.url)
            postIntent.putExtra(PostViewActivity.EXTRA_TITLE, story.title)
            postIntent.putExtra(PostViewActivity.EXTRA_POST_ID, story.entityId)
            startActivity(postIntent)
        }
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

package com.boomapps.steemapp.ui.feeds


import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
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
import com.boomapps.steemapp.ui.ViewState
import com.boomapps.steemapp.ui.dialogs.ProgressFragmentDialog
import com.boomapps.steemapp.ui.dialogs.VotePostDialog
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import com.boomapps.steemapp.ui.editor.inputpostingkey.InputNewPostingKeyActivity
import com.boomapps.steemapp.ui.post.PostViewActivity
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [FeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FeedsFragment : Fragment(), FeedListHolderCallback {


    private enum class LastActions {
        VOTE,
        UNVOTE,
        FLAG
    }

    private var lastAction = LastActions.VOTE

    private val INPUT_NEW_KEY_POST_ACTIVITY_CODE = 22

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
                FeedListHolder(FeedType.FEED, "FEED", View.inflate(context, R.layout.feed_list_view, null), viewModel, this),
                FeedListHolder(FeedType.TRENDING, "TRENDING", View.inflate(context, R.layout.feed_list_view, null), viewModel, this),
                FeedListHolder(FeedType.NEW, "NEW", View.inflate(context, R.layout.feed_list_view, null), viewModel, this)
        ))
        feedsPager.adapter = adapter
        view.findViewById<TabLayout>(R.id.feedsTabs).apply {
            setupWithViewPager(feedsPager)
            tabMode = TabLayout.MODE_SCROLLABLE
        }

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

        viewModel.state.observe(this, Observer<ViewState> { t ->
            when (t) {
                ViewState.COMMON -> {
                    dismissProgress()
                }
                ViewState.PROGRESS -> {
                    showProgress(when (lastAction) {
                        LastActions.VOTE -> "Sending vote ..."
                        LastActions.UNVOTE -> "Canceling vote ..."
                        else -> "Proceed ..."
                    })
                }
                ViewState.FAULT_RESULT -> {
                    viewModel.viewStateProceeded()
                    dismissProgress()
                    if (viewModel.stringError.isNotEmpty()) {
                        val curContext = context
                        if (curContext != null) {
                            WarningDialog.getInstance().showSpecial(curContext, null, viewModel.stringError, "Ok", null, null)
                        }
                    }
                }
                ViewState.SUCCESS_RESULT -> {
                    dismissProgress()
                }
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
            2 ->
                viewModel.showList(FeedType.TRENDING)
            3 ->
                viewModel.showList(FeedType.NEW)
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
            postIntent.putExtra(PostViewActivity.EXTRA_AUTHOR, story.author)
            postIntent.putExtra(PostViewActivity.EXTRA_DATE, story.created) // TODO change
            postIntent.putExtra(PostViewActivity.EXTRA_COMMENTS_NUM, story.commentsNum)
            postIntent.putExtra(PostViewActivity.EXTRA_LINK_NUM, story.linksNum)
            postIntent.putExtra(PostViewActivity.EXTRA_VOTE_NUM, story.votesNum)
            postIntent.putExtra(PostViewActivity.EXTRA_AMOUNT, story.price) // TODO cahange
            postIntent.putExtra(PostViewActivity.EXTRA_AVATAR_URL, story.avatarUrl)
            startActivity(postIntent)
        }
    }


    private fun showInvalidReEnterPostingKeyDialog() {
        val builder = AlertDialog.Builder(context);
        builder
                .setTitle("Oops!!")
                .setMessage("Sorry. You can\'t vote, cause you didn't enter POSTING key before.\n Do you want enter it now?")
                .setPositiveButton("Confirm", { dialog, id ->
                    showScreenForEnterNewPostingKey()
                }).create().show()
    }

    fun showScreenForEnterNewPostingKey() {
        val intent = Intent(context, InputNewPostingKeyActivity::class.java)
        startActivityForResult(intent, INPUT_NEW_KEY_POST_ACTIVITY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == INPUT_NEW_KEY_POST_ACTIVITY_CODE) {
            if (!viewModel.saveNewPostingKey(data)) {
                showInvalidReEnterPostingKeyDialog()
            }
        }
    }

    override fun onActionClick(type: FeedType, position: Int, actions: Actions) {
        // return if cannot find story in DB -> it shouldn't happened ever
        val story = viewModel.getStory(type, position) ?: return
        if (actions == Actions.VOTE) {
            // check is key available
            if (!viewModel.hasPostingKey()) {
                showInvalidReEnterPostingKeyDialog()
                return
            }
            if (story.isVoted) {
                // show unvote confirmation dialog
                val builder = AlertDialog.Builder(context);
                builder
                        .setTitle("Unvote")
                        .setMessage("Are you sure to unvote this post?")
                        .setPositiveButton("Confirm", { dialog, id ->
                            lastAction = LastActions.UNVOTE
                            viewModel.unVote(story, type)

                        })
                        .setNegativeButton("Cancel", { dialog, id ->

                        })
                builder.create().show()
            } else {
                VotePostDialog.newInstance(object : VotePostDialog.OnVotePercentSelectListener {
                    override fun onSelect(value: Int) {
                        lastAction = LastActions.VOTE
                        viewModel.vote(story, type, value)
                    }
                }).show(activity?.supportFragmentManager, VotePostDialog.TAG)
            }
        }
    }


    private fun showProgress(message: String) {
        if (isProgressShowed()) return
        ProgressFragmentDialog.newInstance(message).show(activity?.supportFragmentManager, ProgressFragmentDialog.TAG)
    }

    private fun showProgress(resId: Int) {
        showProgress(getString(resId))
    }

    private fun isProgressShowed(): Boolean {
        val f = activity?.supportFragmentManager?.findFragmentByTag(ProgressFragmentDialog.TAG)
        return f != null && (f.isVisible || f.isAdded)
    }

    private fun dismissProgress() {
        val fragment = activity?.supportFragmentManager?.findFragmentByTag(ProgressFragmentDialog.TAG)
        if (fragment != null) {
            (fragment as ProgressFragmentDialog).dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                FeedsFragment()

        const val KEY_FEED_TYPE = "feed_type"

    }
}

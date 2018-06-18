/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.post

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.BaseActivity
import com.boomapps.steemapp.ui.ViewState
import com.boomapps.steemapp.ui.dialogs.VotePostDialog
import com.boomapps.steemapp.ui.dialogs.WarningDialog
import com.boomapps.steemapp.ui.editor.inputpostingkey.InputNewPostingKeyActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_post.*
import java.util.*

class PostViewActivity : BaseActivity() {

    companion object {
        const val EXTRA_POST_ID = "post_id"
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_AUTHOR = "author"
        const val EXTRA_AVATAR_URL = "avatar_url"
        const val EXTRA_DATE = "date"
        const val EXTRA_COMMENTS_NUM = "comments_num"
        const val EXTRA_LINK_NUM = "link_num"
        const val EXTRA_VOTE_NUM = "vote_num"
        const val EXTRA_AMOUNT = "amount"

    }

    private val INPUT_NEW_KEY_POST_ACTIVITY_CODE = 32

    lateinit var viewModel: PostViewModel
    var pFullWidth = 0

    lateinit var extraUrl: String
    var extraPostId: Long = -1
    var extraTitle = ""
    var extraAvatarUtl = ""
    var extraAuthor = ""
    var extraDate = -1L
    var extraAmount = 0.0f
    var extraCommentNum = 0
    var extraLinksNum = 0
    var extraVoteNum = 0


    private lateinit var progressParams: ConstraintLayout.LayoutParams

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        val bar = supportActionBar
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true)
        }
        extractExtras()
        initPostDataUI()
        title = "FEED"
        viewModel = ViewModelProviders.of(this, PostViewModelFactory(extraPostId, extraUrl, extraTitle)).get(PostViewModel::class.java)
        progressParams = postWebViewProgress.layoutParams as ConstraintLayout.LayoutParams
        initWebView()
        observeState()
        viewModel.fullStoryData.observe(this, Observer {
            if (it?.isVoted == true) {
                aPost_votePriceLayout.setBackgroundResource(R.drawable.bg_feed_card_price_voted_selector)
                aPost_tvFullPrice.setTextColor(ContextCompat.getColorStateList(this, R.color.feed_card_price_unvoted_text_selector))
            } else {
                aPost_votePriceLayout.setBackgroundResource(R.drawable.bg_feed_card_price_unvoted_selector)
                aPost_tvFullPrice.setTextColor(ContextCompat.getColorStateList(this, R.color.feed_card_price_voted_text_selector))
            }
        })
        aPost_votePriceLayout.setOnClickListener {
            if (!viewModel.hasPostingKey()) {
                showInvalidReEnterPostingKeyDialog()
            } else {
                val isVoted = viewModel.isVoted() ?: return@setOnClickListener
                if (isVoted) {
                    showUnVoteConfirmDialog()
                } else {
                    showVoteDialog()
                }

            }

        }

    }

    private fun observeState() {
        viewModel.state.observe(this, Observer<ViewState> { t ->
            when (t) {
                ViewState.COMMON -> {
                    dismissProgress()
                }
                ViewState.PROGRESS -> {
                    val isVoted = viewModel.isVoted() ?: return@Observer
                    showProgress(
                            if (isVoted) {
                                "Canceling vote ..."
                            } else {
                                "Sending vote ..."
                            }
                    )
                }
                ViewState.FAULT_RESULT -> {
                    viewModel.viewStateProceeded()
                    dismissProgress()
                    if (viewModel.stringError.isNotEmpty()) {

                        WarningDialog.getInstance().showSpecial(this, null, viewModel.stringError, "Ok", null, null)
                    }
                }
                ViewState.SUCCESS_RESULT -> {
                    dismissProgress()
                }
            }
        })
    }


    private fun initWebView() {
        postWebView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (right > 0) {
                    pFullWidth = right - left
                    postWebView.removeOnLayoutChangeListener(this)
                }
            }
        })

        postWebView.webViewClient = WebViewClient()// LoadingWebViewClient(loadingListener)
        postWebView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

        }
        postWebView.visibility = View.VISIBLE
        postWebView.settings.javaScriptEnabled = true
        postWebView.settings.loadWithOverviewMode = true

        postWebView.postDelayed({
            viewModel.postData.observe(this, Observer {
                if (it != null) {
                    showHtml(it.body)
                }
            })
        }, 1000)
    }

    private fun extractExtras() {
        val inData = intent
        if (inData != null) {
            extraUrl = inData.getStringExtra(EXTRA_URL)
            extraPostId = inData.getLongExtra(EXTRA_POST_ID, -1L)
            extraTitle = if (inData.hasExtra(EXTRA_TITLE)) {
                inData.getStringExtra(EXTRA_TITLE)
            } else {
                ""
            }
            extraAvatarUtl = inData.getStringExtra(EXTRA_AVATAR_URL)
            extraAuthor = inData.getStringExtra(EXTRA_AUTHOR)
            extraDate = inData.getLongExtra(EXTRA_DATE, -1L)
            extraAmount = inData.getFloatExtra(EXTRA_AMOUNT, 0.0f)
            extraCommentNum = inData.getIntExtra(EXTRA_COMMENTS_NUM, 0)
            extraLinksNum = inData.getIntExtra(EXTRA_LINK_NUM, 0)
            extraVoteNum = inData.getIntExtra(EXTRA_VOTE_NUM, 0)
        }
    }

    private fun initPostDataUI() {
        if (extraAvatarUtl.isNotEmpty()) {
            Glide.with(this)
                    .load(extraAvatarUtl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.circleCropTransform())
                    .into(aPost_ivAuthorAvatar)
        }
        aPost_tvAuthor.text = extraAuthor
        aPost_tvVotesNumber.text = extraVoteNum.toString()
        aPost_tvCommentNumber.text = extraCommentNum.toString()
        aPost_tvLinkNumber.text = extraLinksNum.toString()
        if (extraDate > 0) {
            val created = Calendar.getInstance()
            created.timeInMillis = extraDate
            var sCreated = "unknown"
            if (created.timeInMillis > 0) {
                val curCal = currentDate()
                val yearsDelta = curCal.get(Calendar.YEAR) - created.get(Calendar.YEAR)
                val monthDelta = curCal.get(Calendar.MONTH) - created.get(Calendar.MONTH)
                val dayDelta = curCal.get(Calendar.DAY_OF_YEAR) - created.get(Calendar.DAY_OF_YEAR)
                if (yearsDelta > 0) {
                    aPost_tvLastActivityTime.text = formatDate(yearsDelta, resources.getQuantityString(R.plurals.years, yearsDelta))
                } else if (monthDelta > 0) {
                    aPost_tvLastActivityTime.text = formatDate(monthDelta, resources.getQuantityString(R.plurals.months, monthDelta))
                } else if (dayDelta > 1) {
                    aPost_tvLastActivityTime.text = formatDate(dayDelta, resources.getQuantityString(R.plurals.days, dayDelta))
                } else {
                    aPost_tvLastActivityTime.text = getString(R.string.feed_card_date_format_yesterday)
                }
            }
        }
        aPost_tvFullPrice.text = String.format("$ %.2f", extraAmount)
    }


    private fun currentDate(): Calendar {
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        return c
    }

    private fun formatDate(days: Int, pluralValue: String): String {
        return String.format(getString(R.string.feed_card_date_format_common), days, pluralValue)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPost()
    }

    private fun prettifyHtml(rawBody: String): String {
        if (rawBody.isNotEmpty()) {
            var fullHtml = String.format("%s%s%s", getString(R.string.post_prefix), rawBody, getString(R.string.pos_postfix))
            val userPattern = Regex("\\s(@\\w*)")
            val separateLinksPattern = Regex("<.*>((https?|ftp|file)://.*?)<.*?>")
            val matchResults = userPattern.findAll(fullHtml)
            for (mr in matchResults) {
                val range = mr.range
                val groups = mr.groupValues
            }
            val withUsers = userPattern.replace(fullHtml, { matchResult ->
                val groups = matchResult.groupValues
                val uRes = if (groups.size == 1) {
                    "<a href=\"https://steemit.com/${matchResult.value}\">${matchResult.value}</a>"
                } else {
                    "<a href=\"https://steemit.com/$groups[1]}\">${groups[1]}</a>"
                }

                return@replace uRes
            })
            val withLinks = separateLinksPattern.replace(withUsers, { matchResult ->
                if (matchResult.groupValues.size > 1) {
                    return@replace convertLinkToHtml(matchResult.groupValues[1])
                } else {
                    return@replace convertLinkToHtml(matchResult.value)
                }

            })
            return withLinks

        } else {
            return rawBody
        }
    }

    private fun convertLinkToHtml(inLink: String): String {
        val isExt = inLink.substringAfterLast(".")
        if (isExt.length in 1..4) {
            if (isExt.endsWith("png") or
                    isExt.endsWith("jpg") or
                    isExt.endsWith("jpeg") or
                    isExt.endsWith("gif")) {
                return "<img src=\"$inLink\" alt=\"img${inLink.length}\">"
            }
        }

        return "<a href=\"https://steemit.com/$inLink\">$inLink</a>"
    }


    private fun showHtml(html: String) {
        val sb = StringBuilder()
                .append(getString(R.string.post_new_prefix))
                .append("<h1>").append(extraTitle).append("</h1>")
                .append(html)
                .append(getString(R.string.pos_postfix))
        val fullHtml = sb.toString()
        postWebView.loadData(sb.toString(), "text/html", "UTF-8")
        postWebViewProgress.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (postWebView.canGoBack()) {
            postWebView.goBack()
        } else {
            finish()
        }
    }

    private fun showVoteDialog() {
        VotePostDialog.newInstance(object : VotePostDialog.OnVotePercentSelectListener {
            override fun onSelect(value: Int) {
                viewModel.vote(value)
            }
        }).show(supportFragmentManager, VotePostDialog.TAG)
    }

    private fun showUnVoteConfirmDialog() {
        // show unvote confirmation dialog
        val builder = AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.d_cancel_vote_title))
                .setMessage(getString(R.string.d_cancel_vote_message))
                .setPositiveButton(getString(R.string.d_cancel_vote_btn_ok), { dialog, id ->
                    viewModel.unVote()
                })
                .setNegativeButton(getString(R.string.d_cancel_vote_btn_cancel), { dialog, id ->

                })
        builder.create().show()
    }

    private fun showInvalidReEnterPostingKeyDialog() {
        val builder = AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.d_wron_post_key_title))
                .setMessage(getString(R.string.d_wron_post_key_message))
                .setPositiveButton(getString(R.string.d_wron_post_key_btn_ok), { dialog, id ->
                    showScreenForEnterNewPostingKey()

                })
                .setNegativeButton(getString(R.string.d_wron_post_key_btn_cancel), { dialog, id ->
                    // do nothing
                }).create().show()
    }

    fun showScreenForEnterNewPostingKey() {
        val intent = Intent(this, InputNewPostingKeyActivity::class.java)
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

}
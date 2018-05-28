package com.boomapps.steemapp.ui.post

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_post.*
import timber.log.Timber

class PostViewActivity : BaseActivity() {

    companion object {
        const val EXTRA_POST_ID = "post_id"
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
    }

    lateinit var viewModel: PostViewModel
    var pFullWidth = 0
    private lateinit var progressParams: ConstraintLayout.LayoutParams
    lateinit var extraUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        val bar = supportActionBar
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true)
        }
        viewModel = ViewModelProviders.of(this).get(PostViewModel::class.java)
        progressParams = postWebViewProgress.layoutParams as ConstraintLayout.LayoutParams
        postWebView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {

            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (right > 0) {
                    pFullWidth = right - left
                    postWebView.removeOnLayoutChangeListener(this)
                }
            }
        })
        val inData = intent
        if (inData != null) {
            extraUrl = inData.getStringExtra(EXTRA_URL)
            val extraTitle = if (inData.hasExtra(EXTRA_TITLE)) {
                inData.getStringExtra(EXTRA_TITLE)
            } else {
                ""
            }
            if (title.isNotEmpty()) {
                title = extraTitle
            }
        }
        postWebView.webViewClient = WebViewClient()// LoadingWebViewClient(loadingListener)
        postWebView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress <= 100) {
                    showLoadingProgress(newProgress)
                }
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
//                setTitle(title)
            }
        }
        postWebView.visibility = View.VISIBLE
        postWebView.settings.javaScriptEnabled = true
        postWebView.settings.loadWithOverviewMode = true

        viewModel.postData.observe(this, Observer {
            if (it != null) {
                this@PostViewActivity.title = it.title
                val prettyHtml = prettifyHtml(it.body)
                Timber.d("prettified html : $prettyHtml")
                postWebView.loadData(prettyHtml, "text/html", "UTF-8")
            }
        })

    }

    private fun prettifyHtml(rawBody: String): String {
        if (rawBody.isNotEmpty()) {
            var fullHtml = String.format("%s%s%s", getString(R.string.post_prefix), rawBody, getString(R.string.pos_postfix))
            val userPattern = Regex("\\s(@\\w*)")
            val separateLinksPattern = Regex("<.*>((https?|ftp|file)://.*?)<.*?>")
            val  matchResults = userPattern.findAll(fullHtml)
            for(mr in matchResults){
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


    val loadingListener = object : LoadingWebViewClient.LoadingListener {
        override fun showProgress() {
            setProgressState(true)
        }

        override fun hideProgress() {
            setProgressState(false)
        }

        override fun setTitle(title: String) {
            this@PostViewActivity.title = title
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (intent != null) {
            viewModel.getPost(intent.getLongExtra(EXTRA_POST_ID, -1))
        }
//        postWebView.loadUrl(extraUrl)
    }

    private fun showLoadingProgress(value: Int) {
        progressParams?.width = (pFullWidth / 100f * value).toInt()
        postWebViewProgress.setLayoutParams(progressParams)
    }

    private fun setProgressState(visible: Boolean) {
        postWebViewProgress.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
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

}
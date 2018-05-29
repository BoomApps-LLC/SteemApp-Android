package com.boomapps.steemapp.ui.post

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.ui.BaseViewModel
import com.commonsware.cwac.anddown.AndDown
import com.commonsware.cwac.anddown.AndDown.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import timber.log.Timber

class PostViewModel(val postId: Long, val postUrl: String, val title: String) : BaseViewModel() {

    var postData: LiveData<PostEntity>

    init {
        postData = ServiceLocator.getDaoRepository().getPostLiveData(postId)
    }

    fun getPost(storyId: Long) {
        Timber.d("getPost for $storyId")
        if (postData.value != null) {
            return
        }

        Observable.fromCallable {
            ServiceLocator.getDaoRepository().getStorySync(storyId)
        }
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    if (it != null) {
                        Timber.d("story has been found : ${it.title}")
                    } else {
                        Timber.d("story hasn't been found")
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        val andDown = AndDown()
                        val html = andDown.markdownToHtml(it.rawBody, HOEDOWN_EXT_AUTOLINK and HOEDOWN_EXT_HIGHLIGHT and HOEDOWN_EXT_QUOTE and HOEDOWN_EXT_SPACE_HEADERS, 0)
                        Timber.d("transormed markdown to html = $html")
//                        postData.value = PostData(it.title, html)
                    }
                }, {
                    Timber.e(it, "error getting StoryEntityById")
                }, {

                })
    }

    fun loadPost() {
        if (postData.value != null) {
            return
        }
        Observable.fromCallable {
            getTransformedPage(postUrl)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    if (it != null) {
                        // save it to db
                        val entity = PostEntity()
                        entity.body = it
                        entity.url = postUrl
                        entity.title = title
                        entity.entityId = postId
                        ServiceLocator.getDaoRepository().insertPost(entity)
                    }
                }, {
                    Timber.e(it, "error loading url")
                }, {

                })
    }


    fun getTransformedPage(url: String): String {
        val html = Jsoup.connect(url).get()
        val element = html.body().getElementsByClass("PostFull__body entry-content")
        if (element != null) {
            return element.html()
        } else {
            return html.body().html()
        }

    }

}
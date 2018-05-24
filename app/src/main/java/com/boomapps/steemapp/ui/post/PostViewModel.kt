package com.boomapps.steemapp.ui.post

import android.arch.lifecycle.MutableLiveData
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.ui.BaseViewModel
import com.commonsware.cwac.anddown.AndDown
import com.commonsware.cwac.anddown.AndDown.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class PostViewModel() : BaseViewModel() {

    var postId = MutableLiveData<Long>()
    var postData = MutableLiveData<PostData>()


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
                        postData.value = PostData(it.title, html)
                    }
                }, {
                    Timber.e(it, "error getting StoryEntityById")
                }, {

                })
    }

}
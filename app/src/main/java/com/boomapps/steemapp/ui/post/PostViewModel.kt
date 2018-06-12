package com.boomapps.steemapp.ui.post

import android.arch.lifecycle.LiveData
import android.content.Intent
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.ServiceLocator
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.steem.SteemRepository
import com.boomapps.steemapp.ui.BaseViewModel
import com.boomapps.steemapp.ui.ViewState
import com.commonsware.cwac.anddown.AndDown
import com.commonsware.cwac.anddown.AndDown.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import timber.log.Timber

class PostViewModel(val postId: Long, val postUrl: String, val title: String) : BaseViewModel() {

    var postData: LiveData<PostEntity>

    var fullStoryData: LiveData<StoryEntity>

    init {
        postData = ServiceLocator.getDaoRepository().getPostLiveData(postId)
        fullStoryData = ServiceLocator.getDaoRepository().getStory(postId)
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

    fun isVoted(): Boolean? {
        return fullStoryData.value?.isVoted
    }

    fun unVote() {
        val fullStory = fullStoryData.value ?: return
        state.value = ViewState.PROGRESS
        ServiceLocator.getSteemRepository().unvoteWithUpdate(fullStory, FeedType.getByPosition(fullStory.storyType), object : SteemRepository.Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                state.postValue(ViewState.COMMON)
            }

            override fun onError(error: Throwable) {
                stringError = "Canceling vote error\n${error.message}"
                state.postValue(ViewState.FAULT_RESULT)
            }
        })
    }

    fun vote(percent: Int) {
        val fullStory = fullStoryData.value ?: return
        state.value = ViewState.PROGRESS
        ServiceLocator.getSteemRepository().voteWithUpdate(fullStory, FeedType.getByPosition(fullStory.storyType), percent, object : SteemRepository.Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                state.postValue(ViewState.COMMON)
                Timber.d("VOTE SUCCESS")
            }

            override fun onError(error: Throwable) {
                stringError = "Sending vote Error\n${error.message}"
                state.postValue(ViewState.FAULT_RESULT)
                Timber.d("VOTE ERROR >> ${error.message}")
            }
        })
    }

    fun hasPostingKey(): Boolean {
        return ServiceLocator.getSteemRepository().hasPostingKey()
    }

    fun saveNewPostingKey(data: Intent?): Boolean {
        if (data == null) {
            return false
        }
        if (!data.hasExtra("POSTING_KEY")) {
            return false
        }
        ServiceLocator.getPreferencesRepository().updatePostingKey(data.getStringExtra("POSTING_KEY"))
        return true
    }

}
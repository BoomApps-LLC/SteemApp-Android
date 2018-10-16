/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.post

import androidx.lifecycle.LiveData
import android.content.Intent
import com.boomapps.steemapp.repository.FeedType
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.db.entities.CommentEntity
import com.boomapps.steemapp.repository.db.entities.PostEntity
import com.boomapps.steemapp.repository.db.entities.StoryEntity
import com.boomapps.steemapp.repository.steem.SteemRepository
import com.boomapps.steemapp.ui.BaseViewModel
import com.boomapps.steemapp.ui.ViewState
import timber.log.Timber

class PostViewModel(val postId: Long, val postUrl: String, val title: String) : BaseViewModel() {

    var postData: LiveData<PostEntity>

    var comments : LiveData<Array<CommentEntity>>

    var fullStoryData: LiveData<StoryEntity>

    init {
        postData = RepositoryProvider.getDaoRepository().getPostLiveData(postId)
        fullStoryData = RepositoryProvider.getDaoRepository().getStory(postId)
        comments = RepositoryProvider.getDaoRepository().getCommentsLiveData(postId)
    }

//    fun loadPostWithComments() {
//        if (postData.value != null) {
//            return
//        }
//        Observable.fromCallable {
//            getTransformedPage(postUrl)
//        }
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .subscribe({
//                    if (it != null) {
//                        // save it to db
//                        val entity = PostEntity()
//                        entity.body = it
//                        entity.url = postUrl
//                        entity.title = title
//                        entity.entityId = postId
//                        RepositoryProvider.getDaoRepository().insertPost(entity)
//                    }
//                }, {
//                    Timber.e(it, "error loading url")
//                }, {
//                    // update comments every time
//                    loadComments()
//                })
//    }

//
//    fun getTransformedPage(url: String): String {
//        val html = Jsoup.connect(url).get()
//        val element = html.body().getElementsByClass("PostFull__body entry-content")
//        if (element != null) {
//            return element.html()
//        } else {
//            return html.body().html()
//        }
//
//    }

    fun isVoted(): Boolean? {
        return fullStoryData.value?.isVoted
    }

    fun unVote() {
        val fullStory = fullStoryData.value ?: return
        state.value = ViewState.PROGRESS
        RepositoryProvider.getSteemRepository().unvoteWithUpdate(fullStory, FeedType.getByPosition(fullStory.storyType), object : SteemRepository.Callback<Boolean> {
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
        RepositoryProvider.getSteemRepository().voteWithUpdate(fullStory, FeedType.getByPosition(fullStory.storyType), percent, object : SteemRepository.Callback<Boolean> {
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
        return RepositoryProvider.getSteemRepository().hasPostingKey()
    }

    fun saveNewPostingKey(data: Intent?): Boolean {
        if (data == null) {
            return false
        }
        if (!data.hasExtra("POSTING_KEY")) {
            return false
        }
        // save key in keystore
        RepositoryProvider.getPreferencesRepository().updatePostingKey(data.getStringExtra("POSTING_KEY"))
        // add key into steemJ object
        RepositoryProvider.getSteemRepository().updatePostingKey(data.getStringExtra("POSTING_KEY"))
        return true
    }

    fun loadComments() {
        Timber.d("COMMENTS: loadComments")
        val data = fullStoryData.value ?: return
        RepositoryProvider.getSteemRepository().loadStoryComments(data)
    }


}
package com.boomapps.steemapp.ui.post

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class PostViewModelFactory(val postId: Long, val postUrl: String, val title : String) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PostViewModel(postId, postUrl, title) as T
    }

}
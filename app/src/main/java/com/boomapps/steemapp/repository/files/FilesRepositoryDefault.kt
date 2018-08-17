/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.files

import android.content.Context
import com.boomapps.steemapp.SteemApplication
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.*

/**
 * Created by vgrechikha on 21.03.2018.
 */
class FilesRepositoryDefault : FilesRepository {

    override fun saveStory(value: String, storyCallback: FilesRepository.StoryCallback?) {
        Observable
                .fromCallable({
                    saveStoryToFile(value)
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    storyCallback?.onError()
                }
                .doOnComplete {
                    storyCallback?.onSaveStory()
                }
                .subscribe()
    }

    override fun clearStory(storyCallback: FilesRepository.StoryCallback?) {
        Observable
                .fromCallable({
                    deleteStoryFile()
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    storyCallback?.onError()
                }
                .doOnComplete {
                    storyCallback?.onSaveStory()
                }
                .subscribe()
    }

    override fun loadStory(storyCallback: FilesRepository.StoryCallback) {
        Observable
                .fromCallable({
                    story = readStory()
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    storyCallback.onError()
                }
                .doOnComplete {
                    storyCallback.onLoadStory(story)
                }
                .subscribe()
    }

    var story: String = ""

    private fun saveStoryToFile(value: String) {
        SteemApplication.instance.openFileOutput("story.txt", Context.MODE_PRIVATE).use {
            it.write(value.toByteArray())
        }
    }

    private fun readStory(): String {
        val file = File(SteemApplication.instance.filesDir, "story.txt")
        if (!file.exists() || file.isDirectory || !file.canRead()) {
            return ""
        }
        // check on 1st is file exists
        return SteemApplication.instance.openFileInput("story.txt").use {
            it.bufferedReader().readText()
        }

    }

    private fun deleteStoryFile() {
        SteemApplication.instance.deleteFile("story.txt")
    }

}
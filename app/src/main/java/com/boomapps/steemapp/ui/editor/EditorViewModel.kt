/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.editor

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.RxUtils
import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.StoryInstance
import com.boomapps.steemapp.repository.UserData
import com.boomapps.steemapp.repository.files.FilesRepository
import com.boomapps.steemapp.repository.network.NetworkRepository
import com.boomapps.steemapp.repository.network.NetworkResponseCode
import com.boomapps.steemapp.ui.BaseViewModel
import com.boomapps.steemapp.ui.ViewState
import com.boomapps.steemapp.ui.editor.tabs.CategoryItem
import eu.bittrade.libs.steemj.util.SteemJUtils
import java.net.URL

/**
 * Created by Vitali Grechikha on 24.01.2018.
 */
class EditorViewModel : BaseViewModel() {


    var title: String = ""
    var story: String = ""
    val categories: ArrayList<CategoryItem> = arrayListOf()
    var activeTab: Int = 0
    var rewardPosition: Int = 1
    var upvoteState: Boolean = false
    var uploadedImageUrl: URL? = null
    var successCode: Int = -1

    var uploadTakenPhoto = false
    var uriForUploadingTakenPhoto: Uri? = null
    var uploadPickedPhoto = false
    var sourceUriPickedPhoto: Uri? = null
    var destinationUriPickedPhoto: Uri? = null
    var uploadPhotoUri: Uri? = null
    var lastPostingTime: Long = 0L
    var postingDelay: Long = 0L


    companion object {
        val SUCCESS_IMAGE_UPLOAD = 1111
        val SUCCESS_STORY_UPLOAD = 2222
    }

    override fun onCleared() {
        saveStoryData()
        super.onCleared()
    }


    fun addNewCategory(category: CategoryItem) {
        categories.add(category)
    }

    fun getCategoriesName(): Array<String> {
        val categories: List<String> = categories.map { chipItem -> chipItem.stringValue }
        return categories.toTypedArray()
    }


    fun removeCategory(position: Int) {
        if (categories.size < position) {
            return
        }
        categories.removeAt(position)
    }

    fun prepareTakenPhotoForUploading(data: Intent?, tempImageUris: Array<Uri?>?) {
        val uri = if (data == null) null else Uri.parse(data.toUri(0))
        uriForUploadingTakenPhoto = if (tempImageUris == null || tempImageUris[0] == null) {
            uri
        } else {
            tempImageUris[0]
        }
        uploadTakenPhoto = true
    }


    fun ifReadyForUploadingPhoto(): Boolean {
        return (uploadTakenPhoto && uriForUploadingTakenPhoto != null) ||
                (uploadPickedPhoto && sourceUriPickedPhoto != null && destinationUriPickedPhoto != null)
    }

    fun uploadPreparedPhoto() {
        if (uploadTakenPhoto) {
            state.value = ViewState.PROGRESS
            uploadNewPhoto(uriForUploadingTakenPhoto)
            return
        }
        if (uploadPickedPhoto) {
            state.value = ViewState.PROGRESS
            uploadPickedPhoto()
            return
        }
    }

    fun reUploadPhoto() {
        if (uploadPhotoUri != null) {
            state.value = ViewState.PROGRESS
            uploadNewPhoto(uploadPhotoUri)
        }
    }

    fun uploadNewPhoto(uri: Uri?) {
        if (uri != null) {
            uploadPhotoUri = uri
            val uData = RepositoryProvider.getPreferencesRepository().loadUserData()
            if (uData.postKey == null || uData.postKey.length < 40) {
                uploadTakenPhoto = false
                uploadPickedPhoto = false
                stringError = "photo_upload_empty_key"
                state.value = ViewState.FAULT_RESULT
                saveStoryData()
                return
            }
            RepositoryProvider.getNetworkRepository().uploadNewPhoto(
                    uri,
                    object : NetworkRepository.OnRequestFinishCallback<URL?> {

                        override fun onSuccessRequestFinish(response: URL?) {
                            successCode = SUCCESS_IMAGE_UPLOAD
                            uploadedImageUrl = response
                            state.value = ViewState.SUCCESS_RESULT
                            uploadTakenPhoto = false
                            uploadPickedPhoto = false
                        }

                        override fun onFailureRequestFinish(throwable: Throwable) {
                            if (throwable.message != null) {
                                stringError = throwable.message!!
                            }
                            state.value = ViewState.FAULT_RESULT
                        }
                    })
        } else {
            state.value = ViewState.FAULT_RESULT
        }
    }

    fun prepareForUploadingPickedPhoto(data: Intent?, tempImageUri: Uri?) {
        // source Uri
        sourceUriPickedPhoto = data?.data
        destinationUriPickedPhoto = tempImageUri
        uploadPickedPhoto = true
    }

    fun uploadPickedPhoto() {
        // copy file
        RxUtils().copyFile(sourceUriPickedPhoto!!, destinationUriPickedPhoto!!, object : RxUtils.OnCopyFileResultListener {
            override fun onResult(success: Boolean) {
                if (success) {
                    // call uploading photo
                    uploadNewPhoto(destinationUriPickedPhoto)
                }
            }
        })
    }

    fun saveNewPostingKey(data: Intent?): Boolean {
        if (data == null) {
            return false
        }
        if (!data.hasExtra("POSTING_KEY")) {
            return false
        }
        val repo = RepositoryProvider.getPreferencesRepository()
        val uData = repo.loadUserData()
        val newUserData = UserData(uData.nickname, uData.userName, uData.photoUrl, data.getStringExtra("POSTING_KEY"))
        repo.saveUserData(newUserData)
        return true
    }

    fun publishStory(permlink: String?) {
        state.value = ViewState.PROGRESS
        val rewardsPercent: Short =
                when (rewardPosition) {
                    0 -> 10000
                    1 -> 5000
                    2 -> 0
                    else -> 30000
                }
        if (permlink.isNullOrEmpty()) {
            RepositoryProvider.getNetworkRepository().postStory(title, story, getCategoriesName(), "", rewardsPercent, upvoteState, postCallback)
        } else {
            RepositoryProvider.getNetworkRepository().postStory(title, story, getCategoriesName(), "", rewardsPercent, upvoteState, permlink, postCallback)
        }

    }

    private var postCallback: NetworkRepository.OnRequestFinishCallback<Any?> = object : NetworkRepository.OnRequestFinishCallback<Any?> {
        override fun onSuccessRequestFinish(response : Any?) {
            processSuccessPosting()
        }

        override fun onFailureRequestFinish(code: NetworkResponseCode, throwable: Throwable) {
            if (code == NetworkResponseCode.PERMLINK_DUPLICATE) {
                // repeat request with new generated permlink
                // generate manually new permlink and call postWithKey again
                val newPermLink = SteemJUtils.createPermlinkString(title).plus("-").plus(System.currentTimeMillis().toString())
                publishStory(newPermLink)
            } else {
                saveStoryData()
                Log.d("EditorActivity", "Can't post story >> " + throwable.message)
                stringError = throwable.message ?: "Posting error"
                state.value = ViewState.FAULT_RESULT
            }
        }
    }


    private fun processSuccessPosting() {
        lastPostingTime = System.currentTimeMillis()
        val repo = RepositoryProvider.getPreferencesRepository()
        repo.saveLastTimePosting(lastPostingTime)
        val oldNum = repo.loadSuccessfulPostingNumber()
        repo.saveSuccessfulPostingNumber(oldNum + 1)
        title = ""
        story = ""
        categories.clear()
        saveStoryData()
        successCode = SUCCESS_STORY_UPLOAD
        state.value = ViewState.SUCCESS_RESULT
    }


    fun saveStoryData() {
        RepositoryProvider.getPreferencesRepository().saveStoryData(StoryInstance(title, story, categories))
        RepositoryProvider.getFileRepository().saveStory(story, null)
    }

    fun loadStoryData() {
        if (title.isEmpty() && story.isEmpty() && categories.size == 0) {
            val storyData = RepositoryProvider.getPreferencesRepository().loadStoryData()
            RepositoryProvider.getFileRepository().loadStory(object : FilesRepository.StoryCallback {
                override fun onSaveStory() {

                }

                override fun onError() {
                    Log.d("EditorViewModel", "OnError loading story")
                }

                override fun onClearStory() {

                }

                override fun onLoadStory(story: String) {
                    this@EditorViewModel.story = story
                }
            })
            title = storyData.title
            story = storyData.story
            categories.clear()
            categories.addAll(storyData.categories)
        }
        lastPostingTime = RepositoryProvider.getPreferencesRepository().loadLastTimePosting()
        getDelay()
    }

    fun getDelay(): Long {
        postingDelay = if (lastPostingTime == 0L) {
            0L
        } else {
            val tDelay = System.currentTimeMillis() - lastPostingTime
            Log.d("EditorViewModel", "currentTime=${System.currentTimeMillis()} && lastPostingTime=${lastPostingTime} && tDelay=$tDelay")
            if (tDelay > 5 * 60 * 1000) {
                RepositoryProvider.getPreferencesRepository().saveLastTimePosting(0L)
                0L
            } else {
                5 * 60 * 1000 - tDelay
            }
        }
        return postingDelay
    }


}
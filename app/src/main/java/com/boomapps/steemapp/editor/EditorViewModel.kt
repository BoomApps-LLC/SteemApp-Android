package com.boomapps.steemapp.editor

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.boomapps.steemapp.BaseViewModel
import com.boomapps.steemapp.RxUtils
import com.boomapps.steemapp.SteemApplication
import com.boomapps.steemapp.ViewState
import com.boomapps.steemapp.editor.tabs.CategoryItem
import com.boomapps.steemapp.repository.NetworkRepository
import com.boomapps.steemapp.repository.SharedRepository
import com.boomapps.steemapp.repository.StoryInstance
import java.net.URL

/**
 * Created by Vitali Grechikha on 24.01.2018.
 */
class EditorViewModel : BaseViewModel() {


    var title: String = ""
    var story: String = ""
    val categories: ArrayList<CategoryItem> = arrayListOf()
    var activeTab: Int = 0
    var inputCategory: String = ""
    var rewardPosition: Int = 0
    var upvoteState: Boolean = false
    var uploadedImageUrl: URL? = null
    var successCode: Int = -1

    var uploadTakenPhoto = false
    var uriForUploadingTakenPhoto: Uri? = null
    var uploadPickedPhoto = false
    var sourceUriPickedPhoto: Uri? = null
    var destinationUriPickedPhoto: Uri? = null


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

    fun uploadNewPhoto(uri: Uri?) {
        if (uri != null) {
            NetworkRepository.get().uploadNewPhoto(
                    uri,
                    object : NetworkRepository.OnRequestFinishCallback {

                        override fun onSuccessRequestFinish() {
                            successCode = SUCCESS_IMAGE_UPLOAD
                            uploadedImageUrl = NetworkRepository.get().lastUploadedPhotoUrl
                            state.value = ViewState.SUCCESS_RESULT
                            uploadTakenPhoto = false
                            uploadPickedPhoto = false
                        }

                        override fun onFailureRequestFinish(throwable: Throwable) {
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
        RxUtils().copyFile(sourceUriPickedPhoto!!, destinationUriPickedPhoto!!, SteemApplication.instance, object : RxUtils.OnCopyFileResultListener {
            override fun onResult(success: Boolean) {
                if (success) {
                    // call uploading photo
                    uploadNewPhoto(destinationUriPickedPhoto)
                }
            }
        })
    }

    fun publishStory() {
        state.value = ViewState.PROGRESS
        val rewardsPercent: Short =
                when (rewardPosition) {
                    0 -> 0
                    1 -> 10000
                    2 -> 20000
                    else -> 30000
                }
        NetworkRepository.get().postStory(title, story, getCategoriesName(), "", rewardsPercent, upvoteState, object : NetworkRepository.OnRequestFinishCallback {
            override fun onSuccessRequestFinish() {
                successCode = SUCCESS_STORY_UPLOAD
                state.value = ViewState.SUCCESS_RESULT
                title = ""
                story = ""
                categories.clear()
                saveStoryData()
            }

            override fun onFailureRequestFinish(throwable: Throwable) {
                Log.d("EditorActivity", "can't post story >> " + throwable.localizedMessage)
                stringError = throwable.localizedMessage
                state.value = ViewState.FAULT_RESULT
            }
        })
    }

    private fun saveStoryData() {
        SharedRepository().saveStoryData(StoryInstance(title, story, categories))
    }

    fun loadStoryData() {
        if (title.isEmpty() && story.isEmpty() && categories.size == 0) {
            val storyData = SharedRepository().loadStoryData()
            title = storyData.title
            story = storyData.story
            categories.clear()
            categories.addAll(storyData.categories)
        }
    }

}
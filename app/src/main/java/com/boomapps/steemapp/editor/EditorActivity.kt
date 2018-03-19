package com.boomapps.steemapp.editor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.boomapps.steemapp.*
import com.boomapps.steemapp.controls.ListActionsDialog
import com.boomapps.steemapp.controls.PostingSuccessDialog
import com.boomapps.steemapp.controls.WarningDialog
import com.boomapps.steemapp.editor.inputpostingkey.InputNewPostingKeyActivity
import com.boomapps.steemapp.editor.tabs.*
import kotlinx.android.synthetic.main.activity_editor.*


/**
 * Created by Vitali Grechikha on 20.01.2018.
 */
class EditorActivity : BaseActivity() {

    private lateinit var adapter: EditorTabsAdapter

    lateinit var viewModel: EditorViewModel

    private val INCORRECT_VALUE = -1
    private val IMAGE_TYPE = "image/*"

    val REQUEST_TAKE_PHOTO = 101
    val CHOOSE_PHOTO = 102
    private val PERMISSION_REQUEST_CAMERA = 3546
    private val INPUT_NEW_KEY_POST_ACTIVITY_CODE = 12
    private val INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE = 21


    var pagerHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        viewModel = ViewModelProviders.of(this).get(EditorViewModel::class.java)
        viewModel.loadStoryData()
        viewModel.state.observe(this, Observer<ViewState> { t ->
            when (t) {
                ViewState.COMMON -> {
                    dismissProgress()
                }
                ViewState.PROGRESS -> {
                    showProgress("Processing ...")
                }
                ViewState.FAULT_RESULT -> {
                    viewModel.viewStateProceeded()
                    dismissProgress()
                    if (viewModel.stringError.isNotEmpty()) {
                        showPostingErrorDialog(viewModel.stringError)
                    }
                }
                ViewState.SUCCESS_RESULT -> {
                    dismissProgress()
                    when (viewModel.successCode) {
                        EditorViewModel.SUCCESS_IMAGE_UPLOAD -> {
                            if (viewModel.uploadedImageUrl != null) {
                                storyTab?.insertUploadedImage(viewModel.uploadedImageUrl.toString(), "image_" + System.currentTimeMillis())
                            }
                        }
                        EditorViewModel.SUCCESS_STORY_UPLOAD -> {
                            showPublishSuccessDialog()
                        }
                    }
                }
            }
        })
        adapter = EditorTabsAdapter(
                getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                intArrayOf(R.layout.item_editor_tab_title, R.layout.item_editor_tab_story, R.layout.item_editor_tab_categories, R.layout.item_editor_tab_reward))
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            var curState = SCROLL_STATE_IDLE
            var page = 0

            override fun onPageScrollStateChanged(state: Int) {
                curState = state
                Log.d("onPageChangeLoader", "onPageScrollStateChanged(${state})")
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Log.d("onPageChangeLoader", "onPageScrolled(${position}, ${positionOffset}, ${positionOffsetPixels})")
                if (curState == SCROLL_STATE_IDLE) {
                    onPageSelected(position)
                }
                if (position != page && positionOffset == 0.0f) {
                    page = position
                    Log.d("onPageChangeLoader", "onPageScrolled >> setShowedPage($page)")
                    notifyPagesAboutVisibilityChange(page)
                }
            }

            override fun onPageSelected(position: Int) {
                if (position != 0 && viewModel.activeTab == position) return
                viewModel.activeTab = position
                Log.d("onPageChangeLoader", "onPageSelected(${position})")
                when (position) {
                    0 -> {
                        setUIforTitleTab()
                    }
                    1 -> {
                        setUIforStoryTab()
                    }
                    2 -> {
                        setUIforTagsTab()
                    }
                    3 -> {
                        setUIfor3rdTab()
                    }
                }

            }
        })
        actionButtonTopRight.setOnClickListener({
            if (viewPager.currentItem == 2 && !allowNext) {
                WarningDialog.getInstance().showSpecial(
                        this,
                        null,
                        getString(R.string.warning_one_category_at_least),
                        getString(R.string.button_ok),
                        null,
                        null)
            } else {
                viewPager.setCurrentItem(++viewPager.currentItem, true)
            }
        })

        actionButtonTopLeft.setOnClickListener(onBackClickListener)
        actionButtonTopLeftTitle.setOnClickListener(onBackClickListener)

        Handler().postDelayed({ viewPager.currentItem = viewModel.activeTab }, 300)
        viewPager.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (bottom - top > 0) {
                    pagerHeight = bottom - top
                    viewPager.removeOnLayoutChangeListener(this)
                }
            }
        })
    }


    private fun notifyPagesAboutVisibilityChange(visiblePos: Int) {
        when (visiblePos) {
            0 -> {
                titleTab?.onShow()
                storyTab?.onHide()
            }
            1 -> {
                titleTab?.onHide()
                storyTab?.onShow()
                categoriesTab?.onHide()
            }
            2 -> {
                storyTab?.onHide()
                categoriesTab?.onShow()
                postingTab?.onHide()
            }
            3 -> {
                categoriesTab?.onHide()
                postingTab?.onShow()
            }
        }
    }

    override fun onBackPressed() {
        when (viewPager.currentItem) {
            0 -> {
                // TODO save data
//                viewModel.saveStoryData()
                finish()
            }
            1, 2, 3 -> viewPager.setCurrentItem(--viewPager.currentItem, true)
        }
    }

    private val onBackClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        // save data
//        viewModel.saveStoryData()
        viewPager.adapter = null
        super.onDestroy()
    }

    private var allowNext = false

    private fun setNextControlState(isActive: Boolean) {
        Log.d("EditorActivity", "setNextControlState(${isActive})")
        allowNext = isActive
        if (isActive) {
            actionButtonTopRight.isClickable = true
            actionButtonTopRight.setTextColor(ContextCompat.getColor(this, R.color.green_active))
        } else {
            actionButtonTopRight.setTextColor(ContextCompat.getColor(this, R.color.grey_secondary))
            actionButtonTopRight.isClickable = (viewPager.currentItem == 2/* categories tab to allow show special Warning dialog*/)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> viewModel.prepareTakenPhotoForUploading(data, tempImageUris!!)
                CHOOSE_PHOTO -> viewModel.prepareForUploadingPickedPhoto(data, Utils.get().getNewTempUri())
                INPUT_NEW_KEY_POST_ACTIVITY_CODE -> {
                    if (viewModel.saveNewPostingKey(data)) {
                        viewModel.publishStory()
                    } else {
                        showInvalidReEnterPostingKeyDialog(INPUT_NEW_KEY_POST_ACTIVITY_CODE)
                    }
                }
                INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE -> {
                    if (viewModel.saveNewPostingKey(data)) {
                        viewModel.reUploadPhoto()
                    } else {
                        showInvalidReEnterPostingKeyDialog(INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE)
                    }
                }
            }
        } else {
            if (requestCode == INPUT_NEW_KEY_POST_ACTIVITY_CODE) {
                showInvalidReEnterPostingKeyDialog(INPUT_NEW_KEY_POST_ACTIVITY_CODE)
            } else if (requestCode == INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE) {
                showInvalidReEnterPostingKeyDialog(INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE)
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (viewModel.ifReadyForUploadingPhoto()) {
            viewModel.uploadPreparedPhoto()
        }
    }

    private fun showInvalidReEnterPostingKeyDialog(requestCode: Int) {
        showExtendWarning(
                getString(R.string.warning_wrong_new_posting_key_title),
                getString(R.string.warning_new_empty_posting_key_message),
                getString(R.string.button_yes),
                getString(R.string.button_cancel),
                object : WarningDialog.OnPositiveClickListener {
                    override fun onClick() {
                        showScreenForEnterNewPostingKey(requestCode)
                    }
                })
    }

    fun showPostingErrorDialog(errorMessage: String) {
        var title = getString(R.string.warning_wrong_posting_key_title)
        var message = ""
        if (errorMessage.toLowerCase().contains("private posting key")) {
            // show a dialog with a suggestion to enter a new key
            message = getString(R.string.warning_wrong_postingKey_message)
            showExtendWarning(
                    title,
                    message,
                    getString(R.string.button_yes),
                    getString(R.string.button_cancel),
                    object : WarningDialog.OnPositiveClickListener {
                        override fun onClick() {
                            showScreenForEnterNewPostingKey(INPUT_NEW_KEY_POST_ACTIVITY_CODE)
                        }
                    })
        } else if (viewModel.stringError.toLowerCase().contains("photo_upload_empty_key")) {
            showInvalidReEnterPostingKeyDialog(INPUT_NEW_KEY_PHOTO_ACTIVITY_CODE)
        } else {
            message = "Posting was saved, but not published due to error. " + viewModel.stringError + "."
            val dialog = AlertDialog.Builder(this@EditorActivity).create()
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), { _, _ ->
            })
            dialog.show()
        }


    }

    fun showScreenForEnterNewPostingKey(requestCode: Int) {
        val intent = Intent(this, InputNewPostingKeyActivity::class.java)
        startActivityForResult(intent, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    presenter.onCameraPermissionsGranted()
                } else {
//                    presenter.onCameraPermissionDenied()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }


    private val tabsListener: TabListener = object : TabListener {

        override fun onDataChange(ready: Boolean) {
            setNextControlState(ready)
        }

        override fun onAddPictureClick() {
            showAddNewPictureSourceDialog()
        }
    }


    var titleTab: TitleTab? = null

    fun setUIforTitleTab() {
        Log.d("EditorActivity", "setUIforStoryTab")
        topIndicator_1.setBackgroundResource(R.drawable.drawable_indicator_active)
        topIndicator_2.setBackgroundResource(R.drawable.drawable_indicator_empty)
        topIndicator_3.setBackgroundResource(R.drawable.drawable_indicator_empty)
        topIndicator_4.setBackgroundResource(R.drawable.drawable_indicator_empty)
        actionButtonTopLeft.setImageResource(R.drawable.ic_clear_black_24dp)
        actionButtonTopRight.visibility = View.VISIBLE
        actionButtonTopLeftTitle.setText(R.string.a_editor_action_left_t_title)
        val view = adapter.getViewAtPosition(0)
        if (view != null && titleTab == null) {
            titleTab = TitleTab(view, tabsListener, viewModel)
        }
    }


//    --------------------------- STORY TAB --------------------------------

    var storyTab: StoryTab? = null

    private fun setUIforStoryTab() {
        Log.d("EditorActivity", "setUIforStoryTab")
        topIndicator_1.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_2.setBackgroundResource(R.drawable.drawable_indicator_active)
        topIndicator_3.setBackgroundResource(R.drawable.drawable_indicator_empty)
        topIndicator_4.setBackgroundResource(R.drawable.drawable_indicator_empty)
        actionButtonTopLeft.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        actionButtonTopRight.visibility = View.VISIBLE
        actionButtonTopLeftTitle.setText(R.string.a_editor_action_left_t_story)
        val view = adapter.getViewAtPosition(1)
        if (view != null && storyTab == null) {
            storyTab = StoryTab(view, tabsListener, viewModel)
        }
    }


    var tempImageUris: Array<Uri?>? = null

    private fun showAddNewPictureSourceDialog() {
        //get string resources and show dialog
        val causes = resources
                .obtainTypedArray(R.array.picture_choose_actions)
        val values = ArrayList<String>(causes.length())
        for (i in 0 until causes.length()) {
            val value = getString(
                    causes.getResourceId(i, INCORRECT_VALUE))
            values.add(value)
        }
        causes.recycle()
        val dialog = ListActionsDialog.newInstance(
                getString(R.string.picture_choose_dialog_title),
                values)
        dialog.setOnValuesSelectedListener(object : ListActionsDialog.OnValuesSelectedListener {
            override fun onSelect(position: Int) {
                when (position) {
                    0 -> {
                        tempImageUris = Utils.get().getNewTempUriForExternalApp()
                        if (tempImageUris != null && tempImageUris?.size == 2) {
                            takePhoto(tempImageUris?.get(1))
                        }
                    }
                    1 -> {
                        tempImageUris = Utils.get().getNewTempUriForExternalApp()
                        pickPicture()
                    }
                }
            }
        })
        dialog.onCancel(object : DialogInterface {
            override fun cancel() {

            }

            override fun dismiss() {

            }
        })
        dialog.show(fragmentManager,
                ListActionsDialog.TAG)
    }


    fun takePhoto(pictureUri: Uri?) {
        if (pictureUri == null) {
            return
        }
        // check permissions
        if (!hasCameraPermissions()) {
            requestCameraPermission()
            return
        }
        dispatchTakePictureIntent(pictureUri)
    }

    private fun dispatchTakePictureIntent(pictureUri: Uri) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
            takePictureIntent.flags = FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    private fun hasCameraPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
    }

    fun pickPicture() {
        val pickPictureIntent = Intent(Intent.ACTION_GET_CONTENT, null)
        pickPictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickPictureIntent.type = IMAGE_TYPE
        startActivityForResult(pickPictureIntent, CHOOSE_PHOTO)
    }


//    --------------------------- TAGS TAB --------------------------------

    var categoriesTab: CategoriesTab? = null

    fun setUIforTagsTab() {
        Log.d("EditorActivity", "setUIforTagsTab")
        topIndicator_1.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_2.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_3.setBackgroundResource(R.drawable.drawable_indicator_active)
        topIndicator_4.setBackgroundResource(R.drawable.drawable_indicator_empty)
        actionButtonTopLeft.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        actionButtonTopRight.visibility = View.VISIBLE
        actionButtonTopLeftTitle.setText(R.string.a_editor_action_left_t_tags)
        val view = adapter.getViewAtPosition(2)
        if (view != null && categoriesTab == null) {
            categoriesTab = CategoriesTab(view, tabsListener, viewModel)
        }
    }


//    -------------------------- 3rd tab ----------------------

    var postingTab: PostingTab? = null

    fun setUIfor3rdTab() {
        val focusedView = this.currentFocus
        focusedView?.postDelayed({
            focusedView.hideKeyboard(this)
        }, 300)

        Log.d("EditorActivity", "setUIfor3rdTab")
        topIndicator_1.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_2.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_3.setBackgroundResource(R.drawable.drawable_indicator_filled)
        topIndicator_4.setBackgroundResource(R.drawable.drawable_indicator_active)
        actionButtonTopLeft.setImageResource(R.drawable.ic_arrow_back_black_24dp)
        actionButtonTopRight.visibility = View.INVISIBLE
        actionButtonTopLeftTitle.setText(R.string.a_editor_action_left_t_posing)
        val view = adapter.getViewAtPosition(3)
        if (view != null && postingTab == null) {
            postingTab = PostingTab(view, tabsListener, viewModel)
        }
    }

    private fun showPublishSuccessDialog() {
        val dialog = PostingSuccessDialog()
        dialog.setOnDismissListener(object : PostingSuccessDialog.OnDialogDismissListener {
            override fun onDismiss() {
                finish()
            }
        })
        val fragmentManager = fragmentManager
        dialog.show(fragmentManager, "success_dialog")
    }


}
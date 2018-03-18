package com.boomapps.steemapp.editor.tabs

import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.controls.InsertLinkDialogFragment
import com.boomapps.steemapp.editor.EditorViewModel
import jp.wasabeef.richeditor.RichEditor

/**
 * Created by vgrechikha on 21.02.2018.
 */
class StoryTab(view: View, tabListener: TabListener, viewModel: EditorViewModel) : BaseTabView(view, tabListener, viewModel) {

    lateinit var editor: RichEditor
    lateinit var actionsPanel: HorizontalScrollView

    override fun initComponents() {
        initActions()
        initEditor()
        processFullDataChange()
    }

    override fun onShow() {

    }

    override fun onHide() {

    }

    private fun processFullDataChange(storyLength: Int = -1) {
        val sLength = if (storyLength < 0) {
            viewModel.story.length
        } else {
            storyLength
        }
        dataListener.onDataChange(sLength > 0)
    }

    private fun initEditor() {
        editor = view.findViewById(R.id.editor)
        // have to check is null, cause findViewById can return null
        if (editor != null) {
            editor.setEditorFontSize(14)
            editor.setPadding(0, 0, 0, 10)
            editor.setPlaceholder(view.context.getString(R.string.a_editor_story_text_hint))
            editor.setOnTextChangeListener({ _ ->
                val value = editor.html
                viewModel.story = value
                processFullDataChange(storyLength = value.length)

            })
            editor.html = viewModel.story
            editor.focusEditor()
        }
    }

    private fun initActions() {
        actionsPanel = view.findViewById(R.id.editor_toolbar_sv)
        actionsPanel.findViewById<View>(R.id.actionUndo)?.setOnClickListener { editor.undo() }
        actionsPanel.findViewById<View>(R.id.actionRedo)?.setOnClickListener { editor.redo() }
        actionsPanel.findViewById<View>(R.id.actionTextTypeBold)?.setOnClickListener { editor.setBold() }
        actionsPanel.findViewById<View>(R.id.actionTextTypeItalic)?.setOnClickListener { editor.setItalic() }
        actionsPanel.findViewById<View>(R.id.actionTextTypeUnderline)?.setOnClickListener { editor.setUnderline() }
        actionsPanel.findViewById<View>(R.id.actionTextTypeStrikethrough)?.setOnClickListener { editor.setStrikeThrough() }
        actionsPanel.findViewById<View>(R.id.actionTextTypeH1)?.setOnClickListener { editor.setHeading(1) }
        actionsPanel.findViewById<View>(R.id.actionTextTypeH2)?.setOnClickListener { editor.setHeading(2) }
        actionsPanel.findViewById<View>(R.id.actionTextTypeH3)?.setOnClickListener { editor.setHeading(3) }
        actionsPanel.findViewById<View>(R.id.actionAddBulletList)?.setOnClickListener { editor.setBullets() }
        actionsPanel.findViewById<View>(R.id.actionAddNumberedList)?.setOnClickListener { editor.setNumbers() }
        actionsPanel.findViewById<View>(R.id.actionAddQuote)?.setOnClickListener { editor.setBlockquote() }
//        actionsPanel.findViewById<View>(R.id.actionAlignLeft)?.setOnClickListener { editor.setAlignLeft() }
//        actionsPanel.findViewById<View>(R.id.actionAlignCenter)?.setOnClickListener { editor.setAlignCenter() }
//        actionsPanel.findViewById<View>(R.id.actionAlignRight)?.setOnClickListener { editor.setAlignRight() }
        actionsPanel.findViewById<View>(R.id.actionInsertLine)?.setOnClickListener { editor.insertHorizontalLine() }
        actionsPanel.findViewById<View>(R.id.actionAddLink)?.setOnClickListener {
            val fragment = InsertLinkDialogFragment.newInstance()
            fragment.setOnInsertClickListener(onInsertLinkClickListener)
            // TODO maybe pass call dialog to activity
            fragment.show((view.context as AppCompatActivity).supportFragmentManager, "insert-link-dialog")
        }
        actionsPanel.findViewById<View>(R.id.actionAddImage)?.setOnClickListener { dataListener.onAddPictureClick() }
        actionsPanel.findViewById<View>(R.id.actionClearAll)?.setOnClickListener { editor.html = "" }
    }

    fun insertUploadedImage(url: String, alt: String) {
        editor.insertImage(url, alt)
    }

    private val onInsertLinkClickListener = object : InsertLinkDialogFragment.OnInsertClickListener {
        override fun onInsertClick(title: String, url: String) {
            editor.insertLink(url, title)
        }
    }

}
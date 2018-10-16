/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.ui.editor.tabs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.dialogs.InsertLinkDialogFragment
import com.boomapps.steemapp.ui.editor.EditorViewModel
import jp.wasabeef.richeditor.RichEditor
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import com.boomapps.steemapp.utils.StyledText
import org.jsoup.parser.Tag
import timber.log.Timber


/**
 * Created by vgrechikha on 21.02.2018.
 */
class StoryTab(view: View, tabListener: TabListener, viewModel: EditorViewModel) :
        BaseTabView(view, tabListener, viewModel), RichEditor.OnTextClickListener {

    lateinit var editor: RichEditor
    private lateinit var actionsPanel: HorizontalScrollView
    private lateinit var keyboardButton: ImageView
    lateinit var bold: View
    lateinit var italic: View
    lateinit var underline: View
    private lateinit var strike: View
    private lateinit var h1: View
    private lateinit var h2: View
    private lateinit var h3: View
    private lateinit var bulletList: View
    private lateinit var numberedList: View
    private lateinit var quote: View
    private lateinit var insertLine: View
    private lateinit var link: View

    private var isKeyboardOpened = true
    private val styledText = StyledText()
    private var state: HashMap<String, Boolean> = hashMapOf(
            "bold" to false,
            "italic" to false,
            "underline" to false,
            "strike" to false,
            "h1" to false,
            "h2" to false,
            "h3" to false,
            "bullet_list" to false,
            "numbered_list" to false,
            "quote" to false,
            "insert_line" to false
    )

    override fun initComponents() {
        initActions()
        initEditor()
        processFullDataChange()
    }

    override fun onShow() {
        editor.html = viewModel.story
        val ready = !viewModel.story.isEmpty() and (viewModel.story != "<br><br>")
        dataListener.onDataChange(ready)
    }

    override fun onHide() {

    }

    private fun processFullDataChange(storyLength: Int = -1) {
        val sLength = if (storyLength < 0) {
            viewModel.story.length
        } else {
            storyLength
        }
        val ready = (sLength > 0) and (viewModel.story != "<br><br>")
        dataListener.onDataChange(ready)
    }

    private fun initEditor() {
        editor = view.findViewById(R.id.editor)
        // have to check is null, cause findViewById can return null
        if (editor != null) {
            editor.setEditorFontSize(14)
            editor.setPadding(0, 0, 0, 10)
            editor.setPlaceholder(view.context.getString(R.string.a_editor_story_text_hint))
            editor.setOnTextChangeListener { _ ->
                val value = editor.html
                viewModel.story = value
                processFullDataChange(storyLength = value.length)
            }
            editor.setOnTextClickListener(this)
            editor.html = viewModel.story
            editor.focusEditor()
        }
    }

    private fun initActions() {
        actionsPanel = view.findViewById(R.id.editor_toolbar_sv)
        val undo = actionsPanel.findViewById<View>(R.id.actionUndo)
        undo?.setOnClickListener {
            editor.undo()
        }
        val redo = actionsPanel.findViewById<View>(R.id.actionRedo)
        redo?.setOnClickListener {
            editor.redo()
        }
        bold = actionsPanel.findViewById<View>(R.id.actionTextTypeBold)
        bold.setOnClickListener {
            switchButtonState("bold", bold)
            editor.setBold()
        }
        italic = actionsPanel.findViewById<View>(R.id.actionTextTypeItalic)
        italic.setOnClickListener {
            switchButtonState("italic", italic)
            editor.setItalic()
        }
        underline = actionsPanel.findViewById<View>(R.id.actionTextTypeUnderline)
        underline.setOnClickListener {
            switchButtonState("underline", underline)
            editor.setUnderline()
        }
        strike = actionsPanel.findViewById<View>(R.id.actionTextTypeStrikethrough)
        strike.setOnClickListener {
            switchButtonState("strike", strike)
            editor.setStrikeThrough()
        }
        h1 = actionsPanel.findViewById<View>(R.id.actionTextTypeH1)
        h2 = actionsPanel.findViewById<View>(R.id.actionTextTypeH2)
        h3 = actionsPanel.findViewById<View>(R.id.actionTextTypeH3)
        h1.setOnClickListener {
            switchButtonState("h1", h1)
            setButtonState("h2", h2, false)
            setButtonState("h3", h3, false)
            editor.setHeading(1)
        }
        h2.setOnClickListener {
            switchButtonState("h2", h2)
            setButtonState("h1", h1, false)
            setButtonState("h3", h3, false)
            editor.setHeading(2)
        }
        h3.setOnClickListener {
            switchButtonState("h3", h3)
            setButtonState("h2", h2, false)
            setButtonState("h1", h1, false)
            editor.setHeading(3)
        }
        bulletList = actionsPanel.findViewById<View>(R.id.actionAddBulletList)
        bulletList.setOnClickListener {
            switchButtonState("bullet_list", bulletList)
            editor.setBullets()
        }
        numberedList = actionsPanel.findViewById<View>(R.id.actionAddNumberedList)
        numberedList.setOnClickListener {
            switchButtonState("numbered_list", numberedList)
            editor.setNumbers()
        }
        quote = actionsPanel.findViewById<View>(R.id.actionAddQuote)
        quote.setOnClickListener {
            switchButtonState("quote", quote)
            editor.setBlockquote()
        }
        insertLine = actionsPanel.findViewById<View>(R.id.actionInsertLine)
        insertLine.setOnClickListener {
            editor.insertHorizontalLine()
        }
        link = actionsPanel.findViewById<View>(R.id.actionAddLink)
        link.setOnClickListener {
            val fragment = InsertLinkDialogFragment.newInstance()
            fragment.setOnInsertClickListener(onInsertLinkClickListener)
            // TODO maybe pass call dialog to activity
            fragment.show((view.context as AppCompatActivity).supportFragmentManager, "insert-link-dialog")
        }
        actionsPanel.findViewById<View>(R.id.actionAddImage)?.setOnClickListener { dataListener.onAddPictureClick() }
        actionsPanel.findViewById<View>(R.id.actionClearAll)?.setOnClickListener {
            editor.html = ""
        }

        keyboardButton = view.findViewById(R.id.keyboard_button)
        keyboardButton.setOnClickListener {
            if (isKeyboardOpened) {
                val imm = editor.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editor.windowToken, 0)
            } else {
                val imm = editor.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        initKeyboardStateListener()
    }

    private fun switchButtonState(key: String, view: View) {
        setButtonState(key, view, state[key] != true)
    }

    private fun setButtonState(key: String, view: View, state: Boolean) {
        this.state[key] = state
        val color =  if (this.state[key]!!) {
            R.color.green_active
        } else {
            R.color.black
        }
        ImageViewCompat.setImageTintList(view as ImageButton,
                ColorStateList.valueOf(ContextCompat.getColor(view.context, color)))
    }

    fun insertUploadedImage(url: String, alt: String) {
        editor.insertImage(url, alt)
    }

    private fun initKeyboardStateListener() {
        try {
            view.viewTreeObserver.addOnGlobalLayoutListener {
                val r = Rect()
                view.getWindowVisibleDisplayFrame(r)

                val heightDiff = view.rootView.height - (r.bottom - r.top)
                if (heightDiff > 300) {
                    isKeyboardOpened = true
                    // KEYBOARD IS OPEN
                    keyboardButton.setImageResource(R.drawable.ic_keyboard_arrow_down)
                } else {
                    if (isKeyboardOpened) {
                        isKeyboardOpened = false
                        keyboardButton.setImageResource(R.drawable.ic_keyboard_arrow_up)
                    }
                    // KEYBOARD IS HIDDEN
                }
            }
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }

    private val onInsertLinkClickListener = object : InsertLinkDialogFragment.OnInsertClickListener {
        override fun onInsertClick(title: String, url: String) {
            editor.insertLink(url, title)
        }
    }

    override fun onTextClick(position: Int) {
        val tags = StyledText().findTagsInHtml(position, viewModel.story)
        updateStyleState(tags)
    }

    private fun updateStyleState(tags: List<Tag>) {
        resetButtons()
        for (tag in tags) {
            when (tag.name) {
                 styledText.styledTags.BOLD -> setButtonState(bold)
                 styledText.styledTags.ITALIC -> setButtonState(italic)
                 styledText.styledTags.UNDERLINE -> setButtonState(underline)
                 styledText.styledTags.STRIKE -> setButtonState(strike)
                 styledText.styledTags.H1 -> setButtonState(h1)
                 styledText.styledTags.H2 -> setButtonState(h2)
                 styledText.styledTags.H3 -> setButtonState(h3)
                 styledText.styledTags.BULLET_LIST -> setButtonState(bulletList)
                 styledText.styledTags.NUMBERED_LIST -> setButtonState(numberedList)
                 styledText.styledTags.LINK -> setButtonState(link)
            }
        }
    }

    private fun resetButtons() {
        resetButtonState(bold)
        resetButtonState(italic)
        resetButtonState(underline)
        resetButtonState(strike)
        resetButtonState(h1)
        resetButtonState(h2)
        resetButtonState(h3)
        resetButtonState(bulletList)
        resetButtonState(numberedList)
        resetButtonState(link)
    }

    private fun resetButtonState(view: View) {
        val color = R.color.black
        view.post {
            ImageViewCompat.setImageTintList(view as ImageButton,
                    ColorStateList.valueOf(ContextCompat.getColor(view.context, color)))
        }
    }

    private fun setButtonState(view: View) {
        val color = R.color.green_active
        view.post {
            ImageViewCompat.setImageTintList(view as ImageButton,
                    ColorStateList.valueOf(ContextCompat.getColor(view.context, color)))
        }
    }

    override fun onTextSelect(begin: Int, end: Int) {
        //do nothing
    }

}
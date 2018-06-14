package com.boomapps.steemapp.ui.editor.tabs

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.boomapps.steemapp.R
import com.boomapps.steemapp.ui.editor.EditorViewModel

/**
 * Created by vgrechikha on 03.03.2018.
 */
class TitleTab(view: View, tabListener: TabListener, viewModel: EditorViewModel) : BaseTabView(view, tabListener, viewModel) {


    lateinit var storyTitle: EditText

    override fun initComponents() {
        storyTitle = view.findViewById(R.id.storyTitle)
        storyTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = if (s == null) {
                    ""
                } else {
                    s.toString()
                }
                viewModel.title = value
                dataListener.onDataChange(value.length > 0)
            }
        })
        storyTitle.setText(viewModel.title)
    }


    override fun onShow() {
        storyTitle.setText(viewModel.title)
    }

    override fun onHide() {

    }
}
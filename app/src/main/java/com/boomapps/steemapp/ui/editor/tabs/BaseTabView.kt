package com.boomapps.steemapp.ui.editor.tabs

import android.view.View
import com.boomapps.steemapp.ui.editor.EditorViewModel

/**
 * Created by vgrechikha on 21.02.2018.
 */
abstract class BaseTabView(var view: View, var dataListener: TabListener, var viewModel: EditorViewModel) {
    init {
        initComponents()
    }

    fun updateView(newView: View, newListener: TabListener) {
        view = newView
        dataListener = newListener
        initComponents()
    }

    abstract fun initComponents()

    abstract fun onShow()

    abstract fun onHide()

}
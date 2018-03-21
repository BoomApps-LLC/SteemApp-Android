package com.boomapps.steemapp.editor.tabs

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.boomapps.steemapp.*
import com.boomapps.steemapp.controls.WarningDialog
import com.boomapps.steemapp.editor.ChipItemDecoration
import com.boomapps.steemapp.editor.EditorViewModel
import com.xiaofeng.flowlayoutmanager.Alignment
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager

/**
 * Created by vgrechikha on 21.02.2018.
 */
class CategoriesTab(view: View, tabListener: TabListener, viewModel: EditorViewModel) : BaseTabView(view, tabListener, viewModel) {

    lateinit var inputTag: EditText
    lateinit var categoriesList: RecyclerView
    lateinit var addButton: TextView
    lateinit var adapter: CategoriesAdapter

    override fun initComponents() {
        inputTag = view.findViewById(R.id.hashtagInput)
        if (inputTag == null) {
            // wrong view
            // TODO process this wrong state of views
            return
        }
        addButton = view.findViewById(R.id.addCategoryButton)
        addButton.setOnClickListener(View.OnClickListener {
            createTag()
        })
        if (!(::categoriesList.isInitialized)) {
            categoriesList = view.findViewById(R.id.chipsList)
            val layoutManager = FlowLayoutManager()
            layoutManager.setAlignment(Alignment.LEFT)
            layoutManager.setAutoMeasureEnabled(true)
            categoriesList.setLayoutManager(layoutManager)
            categoriesList.addItemDecoration(ChipItemDecoration())
            adapter = CategoriesAdapter(view.context, object : CategoriesAdapter.OnRemoveItemListener {
                override fun onRemove(position: Int) {
                    viewModel.removeCategory(position)
                }
            })
            categoriesList.adapter = adapter
            adapter.addChips(viewModel.categories)
        }
        // set next button state; it depend on number of tags
        dataListener.onDataChange(
                categoriesList.adapter.itemCount > 0
        )
        // add listener to RecyclerView to control children number for setting Next button state
        categoriesList.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View?) {
                notifyDataChanged()
            }

            override fun onChildViewAttachedToWindow(view: View?) {
                notifyDataChanged()
            }
        })
        inputTag.requestFocus()
        inputTag.showKeyboard(view.context)
        inputTag.setSelection(inputTag.text.lastIndex + 1)
        inputTag.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_UP) {
                return@OnKeyListener when (keyCode) {
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NAVIGATE_NEXT, KeyEvent.KEYCODE_SPACE -> {
                        createTag()
                        v.clearFocus()
                        v.post({
                            v.requestFocus()
                        })
                        false
                    }
                    else -> false
                }
            }
            return@OnKeyListener false
        })
    }

    override fun onShow() {

    }

    override fun onHide() {

    }

    fun notifyDataChanged() {
        dataListener.onDataChange(
                categoriesList.adapter.itemCount > 0
        )
    }

    private fun createTag() {
        if (adapter.itemCount == 5) {
            // notifying about limit
            WarningDialog.getInstance().showSpecial(
                    view.context,
                    null,
                    view.context.getString(R.string.warning_categories_number),
                    view.context.getString(R.string.button_ok),
                    null, null)
            return
        }
        val text = inputTag.text
        if (null != text && text.isNotEmpty()) {
            val tag = text.toString().trim().toLowerCase()
            val item = CategoryItem(tag, getMatColor(MDCOLOR_TYPE))
            viewModel.addNewCategory(item)
            adapter.addCategory(item)
            // create tag and clear input field
            inputTag.setText("")
            notifyDataChanged()

        } else {
            WarningDialog.getInstance().showSpecial(
                    view.context,
                    null,
                    view.context.getString(R.string.warning_category_lenght),
                    view.context.getString(R.string.button_ok),
                    null, null)
        }
    }

    private fun getMatColor(typeColor: String): Int {


        val resources = view.context.resources
        return resources.getMatColor(typeColor)
//        var returnColor = Color.BLACK
//        val arrayId = resources.getIdentifier("mdcolor_" + typeColor, "array", SteemApplication.instance.applicationContext.getPackageName())
//
//        if (arrayId != 0) {
//            val colors = resources.obtainTypedArray(arrayId)
//            val index = (Math.random() * colors.length()).toInt()
//            returnColor = colors.getColor(index, Color.BLACK)
//            colors.recycle()
//        }
//        return returnColor
    }

}

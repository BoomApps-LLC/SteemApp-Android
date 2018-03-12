package com.boomapps.steemapp.editor.tabs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.boomapps.steemapp.R


/**
 * Created by Vitali Grechikha on 23.01.2018.
 */
class CategoriesAdapter(val context: Context, val onRemoveItemListener: OnRemoveItemListener) : RecyclerView.Adapter<CategoryHolder>() {

    interface OnRemoveItemListener {
        fun onRemove(position: Int)
    }

    private val dataset: MutableList<CategoryItem> = mutableListOf()

    private val onRemoveClickListener: CategoryHolder.OnRemoveClickListener = object : CategoryHolder.OnRemoveClickListener {
        override fun onClick(position: Int) {
            if (dataset.lastIndex >= position) {
                dataset.removeAt(position)
                notifyItemRemoved(position)
                onRemoveItemListener.onRemove(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(LayoutInflater.from(context).inflate(R.layout.item_category, null), onRemoveClickListener)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder?.setValue(dataset[position], onRemoveClickListener)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }


    fun addCategory(newItem: CategoryItem) {
        dataset.add(newItem)
        notifyItemInserted(dataset.lastIndex)
    }

    fun addChips(newItems: ArrayList<CategoryItem>) {
        if (newItems.size == 0) {
            return
        }
        val startPos = dataset.size
        dataset.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }


}
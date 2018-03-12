package com.boomapps.steemapp.repository

import com.boomapps.steemapp.editor.tabs.CategoryItem

/**
 * Created by Vitali Grechikha on 26.02.2018.
 */
data class StoryInstance(val title : String, val story : String, val categories : ArrayList<CategoryItem>)
/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository

import com.boomapps.steemapp.ui.editor.tabs.CategoryItem

/**
 * Created by Vitali Grechikha on 26.02.2018.
 */
data class StoryInstance(val title : String, val story : String, val categories : ArrayList<CategoryItem>)
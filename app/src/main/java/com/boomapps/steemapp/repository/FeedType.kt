/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository

enum class FeedType {
    BLOG, FEED, TRENDING, NEW;

    companion object {
        fun getByPosition(position: Int): FeedType {
            return when (position) {
                0 -> BLOG
                1 -> FEED
                2 -> TRENDING
                else -> NEW
            }
        }
    }
}

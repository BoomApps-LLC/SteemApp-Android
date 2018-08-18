package com.boomapps.steemapp.repository.network

enum class NetworkResponseCode {

    SUCCESS,
    UNKNOWN_ERROR,
    CONNECTION_ERROR,
    PERMLINK_DUPLICATE, // the same with steem answers but another repo
    EPTY_POSTING_KEY

}
package com.boomapps.steemapp.repository.steem

import eu.bittrade.libs.steemj.apis.database.models.state.Discussion


fun Discussion.getVotesNum(): Int {
    return if (activeVotes == null || activeVotes !is List<*>) {
        0
    } else {
        activeVotes.filter { it.percent != 0 }.size
    }
}

fun Discussion.getUSDprice(): Float {

    return if (pendingPayoutValue.amount > 0) {
        pendingPayoutValue.toReal().toFloat()
    } else {
        totalPayoutValue.toReal().toFloat() + curatorPayoutValue.toReal().toFloat()
    }
}

fun Discussion.getRepliesNum(): Int {
    return if (replies == null || replies !is List<*>) {
        0
    } else {
        replies.size
    }
}
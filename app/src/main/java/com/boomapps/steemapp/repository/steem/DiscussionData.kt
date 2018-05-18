package com.boomapps.steemapp.repository.steem

import eu.bittrade.libs.steemj.apis.database.models.state.Discussion

data class DiscussionData(val orderId : Int, val discussion : Discussion?)
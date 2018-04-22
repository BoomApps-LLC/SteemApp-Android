package com.boomapps.steemapp.feeds

import android.arch.lifecycle.MutableLiveData
import com.boomapps.steemapp.BaseViewModel

enum class FeedType {
    BLOG, FEED, TRENDING, NEW
}

class FeedsViewModel : BaseViewModel() {

    var feedType = FeedType.BLOG

    var blogData: MutableLiveData<ArrayList<FeedCardViewData>> = MutableLiveData()

    var feedData: MutableLiveData<ArrayList<FeedCardViewData>> = MutableLiveData()

    var trendingData: MutableLiveData<ArrayList<FeedCardViewData>> = MutableLiveData()

    var newData: MutableLiveData<ArrayList<FeedCardViewData>> = MutableLiveData()

    fun getData(type: FeedType): MutableLiveData<ArrayList<FeedCardViewData>> {
        var liveData: MutableLiveData<ArrayList<FeedCardViewData>> = MutableLiveData()
        when (type) {
            FeedType.BLOG -> liveData = blogData
            FeedType.FEED -> liveData = feedData
            FeedType.NEW -> liveData = newData
            FeedType.TRENDING -> liveData = trendingData
        }
        if (liveData.value == null) {
            // TODO load data
        }
        return liveData
    }


}
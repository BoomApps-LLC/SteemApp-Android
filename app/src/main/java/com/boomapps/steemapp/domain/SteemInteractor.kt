package com.boomapps.steemapp.domain

import com.boomapps.steemapp.repository.RepositoryProvider
import com.boomapps.steemapp.repository.steem.SteemWorkerResponse
import eu.bittrade.libs.steemj.apis.follow.model.CommentFeedEntry
import io.reactivex.Observable

class SteemInteractor : SteemUseCases {

    override fun login(provider: RepositoryProvider, nickname: String, postingKey: String?): SteemWorkerResponse {
        return provider.getSteemRepository().login(nickname, postingKey)
    }

    override fun getAllBlogShortList(provider: RepositoryProvider): Observable<ArrayList<CommentFeedEntry>> {
        return provider.getSteemRepository().getFeedList()
    }
}
package com.boomapps.steemapp.repository.feed

import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.CommentFeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import timber.log.Timber

class FeedsRequest(val steemJ: SteemJ?, val userName: String?) {

    var allEntries = arrayListOf<CommentFeedEntry>()

    fun load(): ArrayList<CommentFeedEntry> {
        Timber.d("FeedShortEntriesRequest.load()")
        if (userName.isNullOrEmpty() || steemJ == null) {
            Timber.d("FeedShortEntriesRequest.load() >> userName.isNullOrEmpty() || steemJ == null >> return")
            return allEntries
        }
        // TODO use correct username after testing
        val accountName = AccountName("yuriks2000")
        try {
            val lastEntry = steemJ.getBlogEntries(accountName, 0, 1)
            if (lastEntry.size > 0) {
                val size = lastEntry[0].entryId
                if (size > 32766) { // short limit
                    var startPos = 0
                    while (startPos + 32766 < size) {
                        addToAllEntries(steemJ.getFeed(accountName, startPos, (startPos + 32766).toShort()))
                        startPos = +32766
                    }
                } else {
                    addToAllEntries(steemJ.getFeed(accountName, 0, (size - 1).toShort()))
                }

            }
        } catch (e: SteemCommunicationException) {
            Timber.e(e, "Loading blog entries list error")
        } catch (e: SteemResponseException) {
            Timber.e(e, "Loading blog entries list error")
        }
        return allEntries
    }

    private fun addToAllEntries(part: List<CommentFeedEntry>) {
        for (entry in part) {
            Timber.d("BlogEntry >> %s", entry.toString())
            allEntries.add(entry)
        }
    }

}

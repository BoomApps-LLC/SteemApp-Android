/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.feed

import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry
import eu.bittrade.libs.steemj.apis.follow.model.FeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import timber.log.Timber

class FeedShortEntriesRequest(val steemJ: SteemJ?, val accountName: AccountName?) {

    var allEntries = arrayListOf<FeedShortEntry>()

    fun load(): ArrayList<FeedShortEntry> {
        if (accountName == null || steemJ == null) {
            return allEntries
        }
        try {
            val lastEntry = steemJ.getFeedEntries(accountName, 0, 1)
            if (lastEntry.size > 0) {
                val size = lastEntry[0].entryId
                if (size > 32766) { // short limit
                    var startPos = 0
                    while (startPos + 32766 < size) {
                        addToAllEntries(steemJ.getFeedEntries(accountName, startPos, (startPos + 32766).toShort()))
                        startPos = +32766
                    }
                } else {
                    addToAllEntries(steemJ.getFeedEntries(accountName, 0, (size - 1).toShort()))
                }

            }
        } catch (e: SteemCommunicationException) {
            Timber.e(e, "Loading blog entries list error")
        } catch (e: SteemResponseException) {
            Timber.e(e, "Loading blog entries list error")
        }
        return allEntries
    }

    private fun addToAllEntries(part: List<FeedEntry>) {
        for (entry in part) {
            allEntries.add(FeedShortEntry(
                    entry.entryId,
                    entry.author.name,
                    entry.permlink.link,
                    entry.reblogOn.dateTimeAsTimestamp
            ))
        }
    }

}

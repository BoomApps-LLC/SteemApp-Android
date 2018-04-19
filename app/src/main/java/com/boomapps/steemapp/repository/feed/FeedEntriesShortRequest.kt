package com.boomapps.steemapp.repository.feed

import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry
import eu.bittrade.libs.steemj.apis.follow.model.FeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import timber.log.Timber

class FeedEntriesShortRequest(val steemJ: SteemJ, val userName: String) {

    var allEntries = arrayListOf<FeedEntryShort>()

    fun load() : ArrayList<FeedEntryShort> {
        val accountName = AccountName(userName)
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
        var reblogBy : ArrayList<String> = arrayListOf()
        for (entry in part) {
            if(entry.reblogBy != null){
                entry.reblogBy.forEach { t -> reblogBy.add(t.name) }
            }
            allEntries.add(FeedEntryShort(
                    entry.entryId,
                    entry.author.name,
                    entry.permlink.link,
                    reblogBy.toTypedArray(),
                    entry.reblogOn.dateTimeAsTimestamp
            ))
        }
    }

}

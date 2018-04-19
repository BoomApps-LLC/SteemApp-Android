package com.boomapps.steemapp.repository.blog

import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import timber.log.Timber

class BlogEntriesShortRequest(val steemJ: SteemJ, val userName: String) {

    var allEntries = arrayListOf<BlogEntryShort>()

    fun load(): ArrayList<BlogEntryShort> {
        val accountName = AccountName(userName)
        try {
            val lastEntry = steemJ.getBlogEntries(accountName, 0, 1)
            if (lastEntry.size > 0) {
                val size = lastEntry[0].entryId
                if (size > 32766) { // short limit
                    var startPos = 0
                    while (startPos + 32766 < size) {
                        addToAllEntries(steemJ.getBlogEntries(accountName, startPos, (startPos + 32766).toShort()))
                        startPos = +32766
                    }
                } else {
                    addToAllEntries(steemJ.getBlogEntries(accountName, 0, (size - 1).toShort()))
                }

            }
        } catch (e: SteemCommunicationException) {
            Timber.e(e, "Loading blog entries list error")
        } catch (e: SteemResponseException) {
            Timber.e(e, "Loading blog entries list error")
        }
        return allEntries
    }

    private fun addToAllEntries(part: List<BlogEntry>) {
        for (entry in part) {
            allEntries.add(BlogEntryShort(
                    entry.entryId,
                    entry.author.name,
                    entry.permlink.link,
                    entry.blog.name,
                    entry.reblogOn.dateTimeAsTimestamp
            ))
        }
    }

}

/*
 * Copyright 2018, BoomApps LLC. 
 * All rights reserved.
*/
package com.boomapps.steemapp.repository.steem

import com.boomapps.steemapp.repository.FeedType
import eu.bittrade.libs.steemj.SteemJ
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry
import eu.bittrade.libs.steemj.apis.follow.model.FeedEntry
import eu.bittrade.libs.steemj.base.models.AccountName
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException
import eu.bittrade.libs.steemj.exceptions.SteemResponseException
import timber.log.Timber

private const val UNLIMIT = -1

class FeedShortEntriesRequest(val type: FeedType, var steemJ: SteemJ?, val accountName: AccountName?, val start: Int, val limit: Int) {
    constructor(type: FeedType, steemJ: SteemJ?, accountName: AccountName?) : this(type, steemJ, accountName, 0, UNLIMIT)


    var allEntries = arrayListOf<StoryShortEntry>()

    fun load(): ArrayList<StoryShortEntry> {
        Timber.d("load for $type")
        val steemJInstance = steemJ
        if (accountName == null || steemJInstance == null) {
            return allEntries
        }
        try {
            var size = 0
            if (type == FeedType.FEED) {
                val entry = steemJInstance.getFeedEntries(accountName, 0, 1)
                size = if (entry.isNotEmpty()) {
                    entry[0].entryId
                } else {
                    0
                }
            } else {
                val entry = steemJInstance.getBlogEntries(accountName, 0, 1)
                size = if (entry.isNotEmpty()) {
                    entry[0].entryId
                } else {
                    0
                }
            }
            if (size > 0) {

                if (start >= size) {
                    // return empty list
                    return allEntries
                }
                if (limit != UNLIMIT) {
                    // return part of short entries from start
                    if (type == FeedType.FEED) {
                        addFeedEntries(getFeedEntries(steemJInstance, accountName, start, limit.toShort()))
                    } else {
                        addBlogEntries(getBlogEntries(steemJInstance, accountName, start, limit.toShort()))
                    }
                    return allEntries
                }
                if (size > 32766) { // short limit
                    var startPos = 0
                    while (startPos + 32766 < size) {
                        if (type == FeedType.BLOG) {
                            // BLOG
                            addBlogEntries(getBlogEntries(steemJInstance, accountName, startPos, (startPos + 32766).toShort()))
                        } else {
                            // FEED
                            addFeedEntries(getFeedEntries(steemJInstance, accountName, startPos, (startPos + 32766).toShort()))
                        }
                        startPos = +32766
                    }
                } else {
                    if (type == FeedType.BLOG) {
                        addBlogEntries(getBlogEntries(steemJInstance, accountName, 0, (size - 1).toShort()))
                    } else {
                        addFeedEntries(getFeedEntries(steemJInstance, accountName, 0, (size - 1).toShort()))
                    }

                }

            }
        } catch (e: SteemCommunicationException) {
            Timber.e(e, "Loading blog entries list error")
        } catch (e: SteemResponseException) {
            Timber.e(e, "Loading blog entries list error")
        }
        return allEntries
    }

    private fun addFeedEntries(part: List<FeedEntry>) {
        for (entry in part) {
            Timber.d("addFeedEntries(%d -> %s)", entry.entryId, entry.permlink)
            allEntries.add(StoryShortEntry(
                    entry.entryId,
                    entry.author.name,
                    entry.permlink.link,
                    entry.reblogOn.dateTimeAsTimestamp
            ))
        }
    }

    private fun addBlogEntries(part: List<BlogEntry>) {
        for (entry in part) {
            Timber.d("addBlogEntries(%d -> %s)", entry.entryId, entry.permlink)
            allEntries.add(StoryShortEntry(
                    entry.entryId,
                    entry.author.name,
                    entry.permlink.link,
                    entry.reblogOn.dateTimeAsTimestamp
            ))
        }
    }

    private fun getBlogEntries(steemJ: SteemJ, aName: AccountName, start: Int, limit: Short): List<BlogEntry> {
        return steemJ.getBlogEntries(accountName, start, limit)
    }

    private fun getFeedEntries(steemJ: SteemJ, aName: AccountName, start: Int, limit: Short): List<FeedEntry> {
        return steemJ.getFeedEntries(accountName, start, limit)
    }

}

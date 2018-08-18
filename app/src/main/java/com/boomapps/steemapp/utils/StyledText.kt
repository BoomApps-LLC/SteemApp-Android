/*
 * Copyright 2018, BoomApps LLC.
 * All rights reserved.
*/
package com.boomapps.steemapp.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Tag
import kotlin.collections.ArrayList

/**
 * Created by Anatole Salanevich on 08.08.2018.
 */
class StyledText {

    class StyledTag {
        val BOLD = "b"
        val ITALIC = "i"
        val UNDERLINE = "u"
        val STRIKE = "strike"
        val H1 = "h1"
        val H2 = "h2"
        val H3 = "h3"
        val BULLET_LIST = "ul"
        val NUMBERED_LIST = "ol"
        val LINK = "a"
        val IMAGE = "img"

        val tags = arrayListOf(
                BOLD, ITALIC, UNDERLINE, STRIKE, H1, H2, H3, BULLET_LIST, NUMBERED_LIST, LINK, IMAGE)
    }

    val styledTags = StyledTag()
    private var position = 0
    private var tags = ArrayList<Tag>()

    fun findTagsInHtml(positionInPlainText: Int, html: String): List<Tag> {
        val doc = Jsoup.parse(html)
        val nodes = doc.childNodes()
        position = positionInPlainText
        traverse(nodes, ArrayList())
        return tags
    }

    private fun traverse(nodes: List<Node>, tags: ArrayList<Tag>) {
        for (node in nodes) {
            if (position == 0) break
            if (node is TextNode) {
                val text = node.text()
                for (i in 0 until text.length) {
                    if (--position == 0) {
                        this.tags = tags
                        return
                    }
                }
            } else {
                val newTags = ArrayList<Tag>(tags)
                val tag = (node as Element).tag()
                if (styledTags.tags.contains(tag.name)) {
                    newTags.add(tag)
                }
                traverse(node.childNodes(), newTags)
            }
        }
    }

}
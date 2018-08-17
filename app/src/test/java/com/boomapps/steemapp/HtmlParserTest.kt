package com.boomapps.steemapp

import com.boomapps.steemapp.utils.StyledText
import org.junit.Assert
import org.junit.Test

/**
 * Created by Anatole Salanevich on 14.08.2018.
 */
class HtmlParserTest {

    val html = "<a href=\"http://somegreatsite.com\">Link Name</a>\n" +
            "is a link to another nifty site\n" +
            "<H1>This is a Header</H1>\n" +
            "<H2>This is a Medium Header</H2>\n" +
            "Send me mail at <a href=\"mailto:support@yourcompany.com\">\n" +
            "support@yourcompany.com</a>.\n" +
            "<P> This is a new paragraph!\n" +
            "<P> <B>This is a new paragraph!</B>\n" +
            "<BR> <B><I>This is a new sentence <u>without a paragraph break</u>, in bold italics.</I></B>"
    val text = "Link Name is a link to another nifty site This is a Header This is a Medium Header Send me mail at support@yourcompany.com. " +
            "This is a new paragraph! This is a new paragraph! This is a new sentence without a paragraph break, in bold italics."

    @Test
    fun parseHtmlTest() {
        val position = 212
        val tags = StyledText().findTagsInHtml(position, html)
        val symbol = text[212]
        Assert.assertEquals(symbol, 'r')
        Assert.assertEquals(3, tags.size)
    }

}
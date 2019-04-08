package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

const val CLOSING_BRACE = "\\}"  // Work around lint warning about redundant escape.
const val REGEX_PATTERN = "\\{(\\\\displaystyle.*?)$CLOSING_BRACE"
val displayStyleRegex = Regex(REGEX_PATTERN)


fun removeUnwanted(doc: Document): Document {
    // TODO: Pull these out as XML strings.
    doc.select("table.infobox").remove() // Many types of navboxes and infoboxes
    doc.select("table.navbox-inner").remove()
    doc.select("table.wikitable").remove()
    doc.select("div.mw-normal-catlinks").remove()
    doc.select("table.vertical-navbox").remove()
    doc.select("span.IPA").remove() // Phonetic pronunciation
    doc.select("[href*=Pronunciation_respelling_key]").remove() // Pronunciation
//    doc.select("[href*=cite]").remove() // In-text citations
    return doc
}

fun retrieveTitle(docTitle: String): String {
    return docTitle.replace(" - Wikipedia", "")
}

fun articleStateFromHtml(html: String): ArticleState {
    var doc = Jsoup.parse(html)
    // Remove elements from the navboxes
    doc = removeUnwanted(doc)
    val title = retrieveTitle(doc.title())
    // todo(html): Also get lists and block quotes
    val paragraphs = doc.select("p").map {
        it.text()!!
    }
    return ArticleState(title, paragraphs)
}

fun cleanUtterance(text: String): String {
    return displayStyleRegex.replace(text, "")
}

fun fastForward(inState: ArticleState, position: String): ArticleState {
    var returnArticle = inState
    if (position == utteranceId(returnArticle.current()!!)) {
        return returnArticle
    }
    while (returnArticle.hasNext() && position != utteranceId(returnArticle.current()!!)) {
        returnArticle = returnArticle.next()!!
    }
    if (position != utteranceId(returnArticle.current()!!)) {
        return inState
    }
    return returnArticle
}

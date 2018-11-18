package com.greglaun.lector.ui.speak

import org.jsoup.nodes.Document

fun removeUnwanted(doc: Document): Document {
    // TODO: Pull these out as XML strings.
    doc.select("table.infobox").remove() // Many types of navboxes and infoboxes
    doc.select("table.navbox-inner").remove()
    doc.select("table.wikitable").remove()
    doc.select("div.mw-normal-catlinks").remove()
    doc.select("table.vertical-navbox").remove()
    doc.select("span.IPA").remove() // Phonetic pronunciation
    doc.select("[href*=Pronunciation_respelling_key]").remove() // Pronunciation
    doc.select("[href*=cite]").remove() // In-text citations
    return doc
}

fun retrieveTitle(doc: Document): String {
    return doc.title().replace(" - Wikipedia", "")
}


package com.greglaun.lector.data.net

import com.greglaun.lector.data.whitelist.ProbabilisticSet
import org.jsoup.nodes.Document

object WhitelistHelper {
// For an html document, add all links to the white list.
fun adAllToWhiteList(document : Document, whitelist : ProbabilisticSet<String>) {
    val anchorTags = document.select("a")
    val linkSet = HashSet<String>()
    for (a in anchorTags) {
        linkSet.add(a.attr("abs:href").substringBeforeLast("#"))
    }
    whitelist.addAll(linkSet)
}
}
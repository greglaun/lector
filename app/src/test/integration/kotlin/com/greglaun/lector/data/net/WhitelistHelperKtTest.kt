package com.greglaun.lector.data.net

import com.greglaun.lector.data.container.HashSetProbabillisticSet
import org.jsoup.Jsoup
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistHelperKtTest {
    @Test
    fun testAddAllToWhitelist() {
        val dogArticle = Jsoup.connect("http://www.wikipedia.org/wiki/Dog").get()
        val whitelist = HashSetProbabillisticSet<String>()
        WhitelistHelper.adAllToWhiteList(dogArticle, whitelist)
        assertTrue(whitelist.probablyContains("https://en.wikipedia.org/wiki/Dog_breeding"))
    }
}
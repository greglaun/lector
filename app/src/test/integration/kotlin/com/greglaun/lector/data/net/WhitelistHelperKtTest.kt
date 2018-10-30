package com.greglaun.lector.data.net

import com.greglaun.lector.data.whitelist.GlobalWhitelist
import com.greglaun.lector.data.whitelist.HashSetProbabillisticSet
import org.jsoup.Jsoup
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistHelperKtTest {
    @Test
    fun testHashSetWhitelist() {
        val dogArticle = Jsoup.connect("http://www.wikipedia.org/wiki/Dog").get()
        val whitelist = HashSetProbabillisticSet<String>()
        WhitelistHelper.adAllToWhiteList(dogArticle, whitelist)
        assertTrue(whitelist.probablyContains("https://en.wikipedia.org/wiki/Dog_breeding"))
    }

    @Test
    fun testGlobalWhitelist() {
        val dogArticle = Jsoup.connect("http://www.wikipedia.org/wiki/Dog").get()
        val whitelist = GlobalWhitelist
        WhitelistHelper.adAllToWhiteList(dogArticle, whitelist)
        assertTrue(whitelist.probablyContains("https://en.wikipedia.org/wiki/Dog_breeding"))
    }
}
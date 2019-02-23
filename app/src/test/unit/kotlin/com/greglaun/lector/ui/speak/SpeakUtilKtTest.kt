package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class SpeakUtilKtTest {

    fun String.strip(): String {
        return this.replace("\\s+".toRegex(), " ")
    }

    @Test
    fun retrieveTitle() {
        assertEquals(retrieveTitle("Blue - Wikipedia"), "Blue")
    }

    @Test
    fun removeUnwanted() {
        // Warning: This is essentially a change-detecting test. But it is useful for now.
        val parsedRawHtml = Jsoup.parse(File(
                "./src/test/unit/kotlin/com/greglaun/lector/ui/speak/Banana_wiki.html"),
                null,
                "https://en.wikipedia.org")
        val parsedCleanHtml = Jsoup.parse(File(
                "./src/test/unit/kotlin/com/greglaun/lector/ui/speak/Banana_clean.html"),
                null,
                "https://en.wikipedia.org")
        assertEquals(removeUnwanted(parsedRawHtml).toString().strip(),
                parsedCleanHtml.toString().strip())
    }

    @Test
    fun cleanUtterance() {
        assertEquals(cleanUtterance("{\\displaystyle f(a)}"), "")
    }

    @Test
    fun fastForward() {
        val wikiPath = "./src/test/unit/kotlin/com/greglaun/lector/ui/speak/Banana_wiki.html"
        val html = File(wikiPath).readText()
        val articleState = articleStateFromHtml(html)
        assertEquals(articleState.currentIndex, 0)

        val newState = fastForward(articleState, utteranceId(articleState.next()!!.current()!!))
        assertEquals(newState.currentIndex, 1)

        val noOpState = fastForward(articleState, utteranceId("Bad state"))
        assertEquals(noOpState.currentIndex, 0)
    }
}
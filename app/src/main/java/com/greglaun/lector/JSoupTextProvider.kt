package com.greglaun.lector

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.util.*

class JSoupTextProvider(@field:Volatile private var doc: Document?) : TextProvider {

    var paragraphs: Elements? = null
    var title: String? = null
    var html: String? = null

    init {
        prepareDocument(this.doc)
    }

    fun prepareDocument(document: Document?) {
        // Remove elements from the navboxes
        doc = removeUnwanted(document!!)
        title = retrieveTitle(doc!!)
        html = doc!!.html()
        paragraphs = doc!!.select("p")
    }

    private fun retrieveTitle(doc: Document): String {
        return doc.title().replace(" - Wikipedia", "")
    }

    private fun removeUnwanted(doc: Document): Document {
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

    override fun provideOneText(): String {
        if (paragraphs!!.size <= 0) {
            return TextProvider.END_OF_STREAM
        }
        val result = paragraphs!![0]
        paragraphs!!.remove(result)
        return result.text()
    }

    override fun provideText(m: Int): List<String> {
        val n = Math.min(m, paragraphs!!.size - 1)
        val result = ArrayList<String>()
        var tmp: String
        for (i in 0 until n) {
            tmp = popText(paragraphs!!)
            result.add(tmp)
        }
        result.add(TextProvider.END_OF_STREAM)
        return result
    }

    private fun popText(elementList: Elements): String {
        val tmp: String
        tmp = elementList[0].text()
        elementList.removeAt(0)
        return tmp
    }

    override fun fastForwardTo(place: String): Boolean {
        if (paragraphs!!.size == 0) {
            return false
        }
        val parCopy = Elements(paragraphs!!)
        var test = popText(parCopy)
        while (test != place) {
            test = popText(parCopy)
        }
        if (parCopy.size > 0) {
            paragraphs = parCopy
            return true
        }
        return false
    }

    override fun provideAllTexts(): List<String> {
        return provideText(paragraphs!!.size)
    }

    companion object {
        private val TAG = JSoupTextProvider::class.java.simpleName
        fun createFromUri(url: Uri): Deferred<JSoupTextProvider?> {
            return GlobalScope.async {
                var doc : Document? = null
                try {
                    doc = Jsoup.connect(url.toString()).get()
                } catch (e: IOException) {
                    Log.d(TAG, "Failed to fetch article", e)
                }
                if (doc == null) {
                    null
                } else {
                JSoupTextProvider(doc)
                }
            }
        }
    }

}

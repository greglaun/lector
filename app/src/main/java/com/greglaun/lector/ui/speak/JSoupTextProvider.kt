package com.greglaun.lector.ui.speak

import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class JSoupTextProvider(val jsoupState: JsoupState) : TextProvider {
    var iterator: Iterator<String>? = null

    companion object {
        fun jsoupStateFromDoc(document: Document): JsoupState {
            // Remove elements from the navboxes
            val doc = removeUnwanted(document)
            val title = retrieveTitle(doc)
            val paragraphs = doc!!.select("p")
            return JsoupState(
                    title,
                    paragraphs.map { it ->
                        it.html()!!
                    })
        }

        private val TAG = JSoupTextProvider::class.java.simpleName
        fun jsoupStateFromUrl(urlString: String): Deferred<JsoupState?> {
            return GlobalScope.async {
                var doc: Document? = null
                try {
                    doc = Jsoup.connect(urlString).get()
                } catch (e: IOException) {
                    Log.d(TAG, "Failed to fetch article", e)
                }
                if (doc == null) {
                    null
                } else {
                    jsoupStateFromDoc(doc!!)
                }
            }
        }
    }

    override val title: String
        get() = jsoupState.title

    override fun onUrlChanged(urlString: String): Deferred<Unit> {
        return GlobalScope.async {
            jsoupStateFromUrl(urlString)
            Unit
        }
    }

    override fun getCurrent(): String? {
        return iterator?.next()
    }

    override fun advance() {
        if (iterator == null) {
            return
        }
        if (iterator!!.hasNext()) {
            iterator!!.next()
        }
    }

    override fun fastForwardTo(location: String): Boolean {
        if (iterator == null || !iterator!!.hasNext()) {
            return false
        }
        while(iterator!!.hasNext()) {
            if(location == iterator!!.next()) {
                return true
            }
        }
        return false
    }

    override fun articleIsOver(): Boolean {
        return (iterator != null) && (!iterator!!.hasNext())
    }
}

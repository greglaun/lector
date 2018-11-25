package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.contextToTitle
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


// todo(global state): Move to better place.
class MainPresenter(val view : MainContract.View,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource)
    : MainContract.Presenter {
    val defaultContext = "BAD_CONTEXT"
    private var currentRequestContext = defaultContext // todo(strings): Use user's default page
    private var currentContextReady = CompletableDeferred<Boolean>()

    override fun onAttach() {
        ttsPresenter.onStart()
    }

    override fun onDetach() {
        ttsPresenter.onStop()
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onArticleOver() {
        view.enablePlayButton()
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.speakInLoop()
        view.enablePauseButton()
    }

    override fun stopSpeakingAndEnablePlayButton() {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
    }

    override fun onUrlChanged(urlString: String) {
        computeCurrentContext(urlString)
        view.loadUrl(urlString)
        stopSpeakingAndEnablePlayButton()
        ttsPresenter.onUrlChanged(urlString)
    }

    private fun computeCurrentContext(urlString: String) {
        // todo(caching, REST): Replace this ugliness
        // todo(concurrency): Handle access of currentRequestContext from multiple threads
        GlobalScope.launch {
            if (urlString.contains("index.php?search=")) {
                if (urlString.substringAfterLast("search=") == "") {
                    return@launch
                }
                val client = OkHttpClient().newBuilder()
                        .followRedirects(false)
                        .followSslRedirects(false)
                        .build()
                val request = Request.Builder()
                        .url(urlString)
                        .build()
                val response = client.newCall(request).execute()
                if (response != null) {
                    if (response.isRedirect) {
                        val url = response.networkResponse()?.headers()?.toMultimap()?.get("Location")
                        if (url != null) {
                            currentRequestContext = urlToContext(url.get(0))
                        }
                    }
                }

            } else {
                currentRequestContext = urlToContext(urlString)
            }
        }
    }

    override fun onRequest(url: String): Deferred<Response?> {
        return  responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), currentRequestContext)
    }

    private fun isWikiArticle(urlString: String): Boolean {
        return urlString.contains("wikipedia.org/wiki/")
    }


    override fun saveArticle() {
        responseSource.add(currentRequestContext)
    }

    override fun deleteArticle(url: String) {
        responseSource.delete(urlToContext(url))
    }

    override fun onDisplayReadingList() {
        GlobalScope.launch{
            val readingList = getReadingList()
            view.displayReadingList(getReadingList())
        }
    }

    // todo(data): Replace with live data or some other mechanism
    fun getReadingList(): List<String> {
        val readingList : MutableList<String> = ArrayList()
        for (article in responseSource.iterator()) {
            readingList.add(contextToTitle(article))
        }
        val readOnlyList : List<String> = readingList
        return readOnlyList
    }

    override fun responseSource(): ResponseSource {
        return responseSource
    }
}
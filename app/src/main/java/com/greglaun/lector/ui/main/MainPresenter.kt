package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.contextToTitle
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response

// todo(global state): Move to better place.
class MainPresenter(val view : MainContract.View,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource)
    : MainContract.Presenter {
    private var currentRequestContext = "BAD_CONTEXT" // todo(strings): Use user's default page

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

    override fun onUrlChanged(url: String) {
        // todo(optimization): If urlToContext becomes complicated, move it somewhere else.
        currentRequestContext = urlToContext(url)
        view.loadUrl(url)
        stopSpeakingAndEnablePlayButton()
        ttsPresenter.onUrlChanged(url)
    }

    override fun onRequest(url: String): Deferred<Response?> {
        return  responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), currentRequestContext)
    }

    override fun saveArticle(url: String) {
        responseSource.add(urlToContext(url))
    }

    override fun deleteArticle(url: String) {
        responseSource.delete(urlToContext(url))
    }

    override fun onDisplayReadingList() {
        view.displayReadingList(getReadingList())
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
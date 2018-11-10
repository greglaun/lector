package com.greglaun.lector.ui.main

import com.greglaun.lector.TtsPresenter
import com.greglaun.lector.data.cache.HashMapSavedArticleCache
import com.greglaun.lector.data.cache.ResponseSourceFactory
import com.greglaun.lector.data.cache.WhitelistSavedArticleCache
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.whitelist.HashSetWhitelist
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response
import java.io.File

// todo(global state): Move to better place.
val STARTING_URL_STRING = "https://www.wikipedia.org/wiki/Main_Page"

class MainPresenter(val view : MainContract.View,
                    ttsView : TTSContract.AudioView,
                    cacheDir : File)
    : MainContract.Presenter {
    val ttsPresenter = TtsPresenter(ttsView, this)
    val WIKI_LANGUAGE = "en"
    val whitelist : HashSetWhitelist<String> =  HashSetWhitelist()
    val savedArticleCache = WhitelistSavedArticleCache(HashMapSavedArticleCache(), whitelist)
    val responseSource = ResponseSourceFactory.createResponseSource(savedArticleCache,
            cacheDir)
    var currentRequestContext = "BAD_CONTEXT" // todo(strings): Use user's default page

    override fun onAttach(lectorView: MainContract.View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDetach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onArticleOver() {
        view.enablePlayButton()
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.startSpeaking()
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
        ttsPresenter.onUrlChanged(url)
    }

    override fun onRequest(url: String): Deferred<Response?> {
        return  responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), currentRequestContext)
    }

    override fun saveArticle(url: String) {
        whitelist.add(urlToContext(url))
    }

    override fun deleteArticle(url: String) {
        whitelist.delete(urlToContext(url))
    }

    override fun onDisplayReadingList() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
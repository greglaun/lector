package com.greglaun.lector.ui.main

import com.greglaun.lector.TtsPresenter
import com.greglaun.lector.data.cache.HashMapSavedArticleCache
import com.greglaun.lector.data.cache.ResponseSourceFactory
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
    val ttsPresenter = TtsPresenter(ttsView)
    val WIKI_LANGUAGE = "en"
    val savedArticleCache = HashMapSavedArticleCache()
    val responseSource = ResponseSourceFactory.createResponseSource(savedArticleCache,
            cacheDir)
    val currentRequestContext = "BAD_CONTEXT"

    override fun onAttach(lectorView: MainContract.View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDetach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.startSpeaking()
    }

    override fun onPauseBottonPressed() {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
    }

    override fun onUrlChanged(url: String) {
        view.loadUrl(url)
        ttsPresenter.onUrlChanged(url)
    }

    override fun onRequest(url: String): Deferred<Response?> {
        return  responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), currentRequestContext)
    }

    override fun saveArticle(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteArticle(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDisplayReadingList() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
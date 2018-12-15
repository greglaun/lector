package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.POSITION_BEGINNING
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.TTSContract
import com.greglaun.lector.ui.speak.TtsStateListener
import kotlinx.coroutines.experimental.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response




class MainPresenter(val view : MainContract.View,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource)
    : MainContract.Presenter, TtsStateListener {
    private var currentRequestContext = "MAIN_PAGE"
    private val contextThread = newSingleThreadContext("ContextThread")
    override val readingList = mutableListOf<ArticleContext>()

    override fun onAttach() {
        ttsPresenter.onStart(this)
    }

    override fun onDetach() {
        ttsPresenter.onStop()
    }

    override fun responseSource(): ResponseSource {
        return responseSource
    }

    override fun getLectorView(): MainContract.View? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUtteranceStarted(articleState: ArticleState) {
        view.highlightText(articleState)
    }

    override fun onUtteranceEnded(articleState: ArticleState) {
        view.unhighlightAllText()
    }

    override fun onSpeechStopped() {
        view.enablePlayButton()
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.speakInLoop({
            responseSource.updatePosition(currentRequestContext, it)
        })
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
        GlobalScope.launch {
            var position = POSITION_BEGINNING
            if (responseSource.contains(urlToContext(urlString)).await()) {
                position = responseSource.getArticleContext(urlToContext(urlString))
                        .await().position
            }
            ttsPresenter.onUrlChanged(urlString, position)
        }
    }

    override fun loadFromContext(articleContext: ArticleContext) {
        view.hideReadingListView()
        onUrlChanged("https://en.m.wikipedia.org/wiki/" + articleContext.contextString)
        // ttsPresenter.setPosition(articleContext.position)
    }

    private fun computeCurrentContext(urlString: String) {
        // todo(caching, REST): Replace this ugliness
        // todo(concurrency): Handle access of currentRequestContext from multiple threads
        CoroutineScope(contextThread).launch {
            var computedContext = currentRequestContext
            synchronized(currentRequestContext) {
                computedContext = currentRequestContext
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
                computedContext = currentRequestContext
            }
            if (!responseSource.contains(computedContext).await()) {
                responseSource.add(computedContext)
            }
        }
    }

    override fun onRequest(url: String): Deferred<Response?> {
        var curContext: String? = null
        synchronized(currentRequestContext) {
            curContext = currentRequestContext
        }
        return  responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), curContext!!)
    }

    override fun saveArticle() {
        GlobalScope.launch {
            synchronized(currentRequestContext) {
                responseSource.markPermanent(currentRequestContext)
            }
        }
    }

    override fun deleteRequested(articleContext: ArticleContext) {
        view.confirmMessage("Delete article ${articleContext.contextString}?",
                onConfirmed = {
                    if(it) {
                        GlobalScope.launch {
                            responseSource.delete(articleContext.contextString).await()
                            readingList.remove(articleContext)
                            view.onReadingListChanged()
                        }
                    }
                })
    }

    override fun onDisplayReadingList() {
        GlobalScope.launch{
            readingList.clear()
            readingList.addAll(responseSource.getAllPermanent().await())
            view.onReadingListChanged()
            view.displayReadingList()
        }
    }

    override fun onRewindOne() {
        ttsPresenter.reverseOne {it ->
            view.unhighlightAllText()
            view.highlightText(it)
        }
    }

    override fun onForwardOne() {
        ttsPresenter.advanceOne {it ->
            view.unhighlightAllText()
            view.highlightText(it)
        }
    }
}
package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.*
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.net.DownloadCompleter
import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainPresenter(val view : MainContract.View,
                    val store: Store,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource,
                    val courseSource: CourseSource)
    : MainContract.Presenter, TtsStateListener, StateHandler {
    override var downloadCompleter: DownloadCompleter? = null
    private val contextThread = newSingleThreadContext("ContextThread")
    private var downloadScheduler: DownloadCompletionScheduler? = null
    private var articleStateSource: ArticleStateSource? = null


    // todo(data): Replace readingList and courseList with LiveData?
    override val readingList = mutableListOf<ArticleContext>()
    override val courseList = mutableListOf<CourseContext>()

    // Preferences
    // todo(state): Find a better solution to preferences that doesn't depend on Android libs
    private var autoPlay = true
    private var autoDelete = true

    private var isActivityRunning = false

    override fun onAttach() {
        ttsPresenter.onStart(this)
        downloadCompleter?.let {
            downloadScheduler = DownloadCompletionScheduler(downloadCompleter!!, responseSource)
            downloadScheduler?.startDownloads()
        }
        articleStateSource = JSoupArticleStateSource(responseSource)
        store.stateHandlers.add(this)
        isActivityRunning = true
        handleState(store.state)
    }

    override fun onDetach() {
        ttsPresenter.onStop()
        downloadScheduler?.stopDownloads()
        store.stateHandlers.remove(this)
    }

    override suspend fun handle(state: State) {
        if (isActivityRunning) {
            handleState(state)
        }
    }

    private fun handleState(state: State) {
        when (state.navigation) {

            Navigation.CURRENT_ARTICLE -> {
                handleCurrentArticle(state)
            }

            Navigation.BROWSE_COURSES -> {
                val currentCourse = state.currentArticleScreen.currentCourse
                courseSource.getArticlesForCourse(currentCourse.title)?.let {
                    displayArticleList(it,
                           courseName)
                }
            }

            Navigation.NEW_ARTICLE -> {
                handleNewArticle(state)
            }

            else -> throw NotImplementedError()
        }
    }

    private fun handleCurrentArticle(state: State) {
        throw NotImplementedError()
    }

    private fun handleNewArticle(state: State) {
        throw NotImplementedError()
    }

    override fun getLectorView(): MainContract.View? {
        return view
    }

    override fun responseSource(): ResponseSource {
        return responseSource
    }

    override fun courseSource(): CourseSource {
        return courseSource
    }

    override fun onUtteranceStarted(articleState: ArticleState) {
        view.highlightText(articleState)
    }

    override fun onUtteranceEnded(articleState: ArticleState) {
        view.unhighlightAllText()
    }

    override suspend fun onArticleFinished(articleState: ArticleState) {
        if (autoPlay) {
            autoPlayNext(articleState)
        }
        if (autoDelete) {
            autoDeleteCurrent(articleState)
        }
    }

    private fun autoDeleteCurrent(articleState: ArticleState) {
        GlobalScope.launch {
            responseSource.delete(articleState.title)
        }
    }

    private suspend fun autoPlayNext(articleState: ArticleState) {
        var nextArticle: ArticleContext? = null
        if (store.state.currentArticleScreen.currentContext == DEFAULT_ARTICLE) {
            nextArticle = responseSource.getNextArticle(articleState.title)
        } else {
            nextArticle = courseSource.getNextInCourse(
                    store.state.currentArticleScreen.currentContext, articleState.title)
        }
        nextArticle?.let {
            onUrlChanged(contextToUrl(it.contextString))
            onPlayButtonPressed()
        }
    }

    override fun onSpeechStopped() {
        view.enablePlayButton()
    }

    override fun onPlayButtonPressed() {
        ttsPresenter.speakInLoop({
            GlobalScope.launch {
                store.dispatch(UpdateArticleAction(updatePosition()))
            }
        })
        view.enablePauseButton()
    }

    override fun stopSpeakingAndEnablePlayButton() {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
    }

    override suspend fun onUrlChanged(urlString: String) {
        computeCurrentContext(urlString)
        view.loadUrl(urlString)
        stopSpeakingAndEnablePlayButton()
        var position = POSITION_BEGINNING
        if (responseSource.contains(urlToContext(urlString))) {
            responseSource.getArticleContext(urlToContext(urlString))?.let {
                position = it.position
            }
        }
        articleStateSource?.getArticle(urlString)?.let {
            ttsPresenter.onArticleChanged(fastForward(it, position))
            if (it.title != store.state.currentArticleScreen.currentContext) {
                val previousTitle = store.state.currentArticleScreen.currentContext
                store.dispatch(UpdateArticleAction(it))
                GlobalScope.launch {
                    responseSource.update(previousTitle, it.title)
                }
            }
        }
    }

    override suspend fun loadFromContext(articleContext: ArticleContext) {
        onUrlChanged(contextToUrl(articleContext.contextString))
        view.unhideWebView()
    }

    private fun computeCurrentContext(urlString: String) {
        // todo(caching, REST): Replace this ugliness
        // todo(concurrency): Handle access of store.state.currentArticleScreen.currentContext from multiple threads
        CoroutineScope(contextThread).launch {
            var computedContext = store.state.currentArticleScreen.currentContext
            synchronized(store.state.currentArticleScreen.currentContext) {
                computedContext = store.state.currentArticleScreen.currentContext
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
                            val url = response.networkResponse()?.headers()?.toMultimap()?.
                                    get("Location")
                            if (url != null) {
                                GlobalScope.launch {
                                    store.dispatch(UpdateArticleAction(articleStatefromTitle(
                                            urlToContext(url.get(0)))))
                                }
                            }
                        }
                    }
                } else {
                    GlobalScope.launch {
                        store.dispatch(UpdateArticleAction(articleStatefromTitle(
                                urlToContext(urlString))))
                    }
                }
                computedContext = store.state.currentArticleScreen.currentContext
            }
            if (!this@MainPresenter.responseSource.contains(computedContext)) {
                responseSource.add(computedContext)
            }
        }
    }

    override suspend fun onRequest(url: String): Response? {
        var curContext: String? = null
        synchronized(store.state.currentArticleScreen.currentContext) {
            curContext = store.state.currentArticleScreen.currentContext
        }
        return responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), curContext!!)
    }

    override suspend fun saveArticle() {
        val requestContextCopy = store.state.currentArticleScreen.currentContext
        responseSource.markPermanent(requestContextCopy)
    }

    override suspend fun courseDetailsRequested(courseContext: CourseContext) {
        courseContext.id?.let {
            store.dispatch(CourseDetailRequestAction(courseContext))
        }
    }

    override fun deleteRequested(articleContext: ArticleContext) {
        view.confirmMessage("Delete article ${articleContext.contextString}?",
                onConfirmed = {
                    if (it) {
                        GlobalScope.launch {
                            responseSource.delete(articleContext.contextString)
                            readingList.remove(articleContext)
                            view.onReadingListChanged()
                        }
                    }
                })
    }

    override fun deleteRequested(courseContext: CourseContext) {
        view.confirmMessage("Delete course ${courseContext.courseName}?",
                onConfirmed = {
                    if (it) {
                        GlobalScope.launch {
                            courseSource.delete(courseContext.courseName)
                            courseList.remove(courseContext)
                            view.onCoursesChanged()
                        }
                    }
                })
    }

    override suspend fun onDisplayReadingList() {
        responseSource.getAllPermanent()?.let {
            displayArticleList(it, store.state.currentArticleScreen.currentCourse)
        }
    }

    private fun displayArticleList(articleList: List<ArticleContext>, title: String? = null) {
        readingList.clear()
        readingList.addAll(articleList)
        view.onReadingListChanged()
        view.displayReadingList(title)
    }

    override suspend fun onDisplayCourses() {
        courseList.clear()
        courseSource.getCourses()?.let {
            courseList.addAll(it)
        }
        view.onCoursesChanged()
        view.displayCourses()
    }

    override fun onRewindOne() {
        ttsPresenter.reverseOne { it ->
            view.unhighlightAllText()
            view.highlightText(it)
        }
    }

    override fun onForwardOne() {
        ttsPresenter.advanceOne { it ->
            view.unhighlightAllText()
            view.highlightText(it)
        }
    }

    override fun setHandsomeBritish(shouldBeBritish: Boolean) {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
        ttsPresenter.setHandsomeBritish(shouldBeBritish)
    }

    override fun setSpeechRate(speechRate: Float) {
        ttsPresenter.stopSpeaking()
        view.enablePlayButton()
        ttsPresenter.setSpeechRate(speechRate)
    }

    override fun evaluateJavascript(js: String, callback: ((String) -> Unit)?) {
        view.evaluateJavascript(js, callback)
    }

    override suspend fun onPageDownloadFinished(urlString: String) {
        responseSource.markFinished(urlToContext(urlString))
    }

    override fun playAllPressed(title: String) {
        // todo: we are going to have to handle the notion of autoplay being temporarily enabled,
        // todo: or else change the language from "play all" to "start playing" or something else
        if (readingList != null && readingList.size > 0) {
            GlobalScope.launch {
                onUrlChanged(contextToUrl(readingList[0].contextString))
                onPlayButtonPressed()
            }
        }
    }

    override fun setAutoPlay(autoPlayIn: Boolean) {
        autoPlay = autoPlayIn
    }

    override fun setAutoDelete(autoDeleteIn: Boolean) {
        autoDelete = autoDeleteIn
    }

    private fun updatePosition(): ArticleState {
        if (store.state.currentArticleScreen.articleState.hasNext()) {
            return store.state.currentArticleScreen.articleState.next()!!
        } else {
            return store.state.currentArticleScreen.articleState
        }
    }
}
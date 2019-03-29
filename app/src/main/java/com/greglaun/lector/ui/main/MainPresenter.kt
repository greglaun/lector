package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.*
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.*
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.Response

class MainPresenter(val view : MainContract.View,
                    val store: Store,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource,
                    val courseSource: CourseSource)
    : MainContract.Presenter, TtsStateListener, StateHandler {
    private var articleStateSource: ArticleStateSource? = null

    // todo(data): Replace readingList and courseList with LiveData?
    override val readingList = mutableListOf<ArticleContext>()
    override val courseList = mutableListOf<CourseContext>()

    private var autoPlay = true
    private var autoDelete = true

    private var isActivityRunning = false

    override fun onAttach() {
        ttsPresenter.deprecatedOnStart(this)
        articleStateSource = JSoupArticleStateSource(responseSource)
        store.stateHandlers.add(this)
        isActivityRunning = true
        handleState(store.state)
        GlobalScope.launch {
            store.dispatch(ReadAction.StartDownloadAction())
        }
    }

    override fun onDetach() {
        ttsPresenter.deprecatedOnStop()
        runBlocking {
            store.dispatch(ReadAction.StopDownloadAction())
        }
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
                GlobalScope.launch {
                    courseSource.getArticlesForCourse(currentCourse.id!!).let {
                        displayArticleList(it,
                                currentCourse.courseName)
                    }
                }
            }
            Navigation.NEW_ARTICLE -> {
                handleNewArticle(state)
            }

            else -> throw NotImplementedError()
        }
    }

    private fun handleCurrentArticle(state: State) {
        if (state.speakerState != SpeakerState.SPEAKING_NEW_UTTERANCE &&
                state.speakerState != SpeakerState.SPEAKING) {
            view.unhighlightAllText()
            view.enablePlayButton()
        }
        if (state.speakerState == SpeakerState.SPEAKING_NEW_UTTERANCE) {
            view.enablePauseButton()
            view.unhighlightAllText()
            view.highlightText(state.currentArticleScreen.articleState as ArticleState)
        }
    }

    private fun handleNewArticle(state: State) {
        view.loadUrl(contextToUrl(state.currentArticleScreen.articleState.title))
        GlobalScope.launch {
            // todo(refactoring): Is this the right way to handle confirming article loading?
            store.dispatch(UpdateAction.UpdateNavigationAction(Navigation.CURRENT_ARTICLE))
        }
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

    override fun onPlayButtonPressed() {
        GlobalScope.launch {
            ttsPresenter.deprecatedOnArticleChanged(
                    store.state.currentArticleScreen.articleState as ArticleState)
            ttsPresenter.startSpeaking({
            GlobalScope.launch {
                store.dispatch(UpdateAction.UpdateArticleAction(updatePosition()))
            }
        })}
    }

    override fun stopSpeakingAndEnablePlayButton() {
        runBlocking {
            store.dispatch(SpeakerAction.StopSpeakingAction())
        }
        view.enablePlayButton()
    }

    override suspend fun onUrlChanged(urlString: String) {
        GlobalScope.launch {
            store.dispatch(ReadAction.LoadNewUrlAction(urlString))
        }
    }

    override suspend fun loadFromContext(articleContext: ArticleContext) {
        onUrlChanged(contextToUrl(articleContext.contextString))
        view.unhideWebView()
    }

    override suspend fun onRequest(url: String): Response? {
        var curContext: String? = null
        synchronized(store.state.currentArticleScreen.articleState.title) {
            curContext = store.state.currentArticleScreen.articleState.title
        }
        return responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), curContext!!)
    }

    override suspend fun saveArticle() {
        val requestContextCopy = store.state.currentArticleScreen.articleState.title
        responseSource.markPermanent(requestContextCopy)
    }

    override suspend fun courseDetailsRequested(courseContext: CourseContext) {
        courseContext.id?.let {
            store.dispatch(ReadAction.FetchCourseDetailsAction(courseContext))
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
            displayArticleList(it, store.state.currentArticleScreen.currentCourse.courseName)
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

    override suspend fun onRewindOne() {
        ttsPresenter.backOne()
    }

    override suspend fun onForwardOne() {
        ttsPresenter.forwardOne()
    }

    override fun setHandsomeBritish(shouldBeBritish: Boolean) {
        runBlocking {
            ttsPresenter.stopSpeaking()
        }
        view.enablePlayButton()
        ttsPresenter.deprecatedHandsomeBritish(shouldBeBritish)
    }

    override fun setSpeechRate(speechRate: Float) {
        runBlocking {
            ttsPresenter.stopSpeaking()
        }
        view.enablePlayButton()
        ttsPresenter.deprecatedSetSpeechRate(speechRate)
    }

    override fun evaluateJavascript(js: String, callback: ((String) -> Unit)?) {
        view.evaluateJavascript(js, callback)
    }

    override suspend fun onPageDownloadFinished(urlString: String) {
        responseSource.markFinished(urlToContext(urlString))
    }

    override fun playAllPressed(title: String) {
        if (readingList.size > 0) {
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

    private fun updatePosition(): AbstractArticleState {
        if (store.state.currentArticleScreen.articleState.hasNext()) {
            return store.state.currentArticleScreen.articleState.next()!!
        } else {
            return store.state.currentArticleScreen.articleState
        }
    }
}
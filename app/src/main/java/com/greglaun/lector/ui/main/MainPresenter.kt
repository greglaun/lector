package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.*
import com.greglaun.lector.data.course.ConcreteCourseContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseDescription
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.net.DownloadCompleter
import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.TTSContract
import com.greglaun.lector.ui.speak.TtsStateListener
import kotlinx.coroutines.experimental.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainPresenter(val view : MainContract.View,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource,
                    val courseSource: CourseSource)
    : MainContract.Presenter, TtsStateListener {
    override var downloadCompleter: DownloadCompleter? = null
    private var currentRequestContext = "MAIN_PAGE"
    private val contextThread = newSingleThreadContext("ContextThread")
    private var downloadScheduler: DownloadCompletionScheduler? = null

    // todo(data): Replace readingList and courseList with LiveData?
    override val readingList = mutableListOf<ArticleContext>()
    override val courseList = mutableListOf<CourseContext>()

    // Preferences
    // todo(state): Find a better solution to preferences that doesn't depend on Android libs
    private var autoPlay = true
    private var autoDelete = true

    override fun onAttach() {
        ttsPresenter.onStart(this)
        downloadCompleter?.let {
            downloadScheduler = DownloadCompletionScheduler(downloadCompleter!!, responseSource)
            downloadScheduler?.startDownloads()
        }
    }

    override fun onDetach() {
        ttsPresenter.onStop()
        downloadScheduler?.stopDownloads()
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

    override fun onArticleFinished(articleState: ArticleState) {
        GlobalScope.launch {
            if (autoDelete) {
                launch {
                    responseSource.delete(articleState.title)
                }
            }
            if (autoPlay) {
                val nextArticle = responseSource.getNextArticle(articleState.title).await()
                nextArticle?.let {
                    onUrlChanged(contextToUrl(it.contextString)).await()
                    onPlayButtonPressed()
                }
            }
        }
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

    override fun onUrlChanged(urlString: String): Deferred<Unit> {
        return GlobalScope.async {
            computeCurrentContext(urlString)
            view.loadUrl(urlString)
            stopSpeakingAndEnablePlayButton()
            var position = POSITION_BEGINNING
            if (responseSource.contains(urlToContext(urlString)).await()) {
                responseSource.getArticleContext(urlToContext(urlString))
                        .await()?.let{
                            position = it.position
                        }
            }
            ttsPresenter.onUrlChanged(urlString, position)
        }
    }

    override fun loadFromContext(articleContext: ArticleContext) {
        onUrlChanged(contextToUrl(articleContext.contextString))
        view.unhideWebView()
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

    override fun courseDetailsRequested(courseContext: CourseContext) {
        GlobalScope.launch {
            courseContext.id?.apply {
                val articlesForCourse = courseSource.getArticlesForCourse(this).await()
                displayArticleList(articlesForCourse)
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

    override fun deleteRequested(courseContext: CourseContext) {
        view.confirmMessage("Delete course ${courseContext.courseName}?",
                onConfirmed = {
                    if(it) {
                        GlobalScope.launch {
                            courseSource.delete(courseContext.courseName).await()
                            courseList.remove(courseContext)
                            view.onCoursesChanged()
                        }
                    }
                })
    }

    override fun onDisplayReadingList() {
        GlobalScope.launch{
            val articleList = responseSource.getAllPermanent().await()
            displayArticleList(articleList)
        }
    }

    private fun displayArticleList(articleList: List<ArticleContext>) {
        readingList.clear()
        readingList.addAll(articleList)
        view.onReadingListChanged()
        view.displayReadingList()
    }

    override fun onDisplayCourses() {
        GlobalScope.launch{
            courseList.clear()
            courseList.addAll(courseSource.getCourses().await())
            view.onCoursesChanged()
            view.displayCourses()
        }
    }

    fun addCourse(courseDescription: CourseDescription) {
        runBlocking {
            val courseId = courseSource.add(ConcreteCourseContext(null,
                    courseDescription.courseName,
                    0)).await()
            courseDescription.articleUrls.map {
                async {
                    responseSource.add(urlToContext(it)).await()
                    courseSource.addArticleForSource(courseDescription.courseName,
                            urlToContext(it)).await()
                }
            }.forEach {it.await() }
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

    override fun onPageDownloadFinished(urlString: String) {
        responseSource.markFinished(urlToContext(urlString))
    }

    override fun setAutoPlay(autoPlayIn: Boolean) {
        autoPlay = autoPlayIn
    }

    override fun setAutoDelete(autoDeleteIn: Boolean) {
        autoDelete = autoDeleteIn
    }
}
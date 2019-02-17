package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.*
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.net.DownloadCompleter
import com.greglaun.lector.data.net.DownloadCompletionScheduler
import com.greglaun.lector.ui.speak.*
import kotlinx.coroutines.experimental.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainPresenter(val view : MainContract.View,
                    val ttsPresenter: TTSContract.Presenter,
                    val responseSource: ResponseSource,
                    val courseSource: CourseSource)
    : MainContract.Presenter, TtsStateListener {
    override val LECTOR_UNIVERSE = ""
    override val ALL_ARTICLES = "All Articles"

    // Mutable state
    private var currentRequestContext = "MAIN_PAGE"
    private var currentCourse = LECTOR_UNIVERSE // Be default, the "course" is everything

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

    override fun onAttach() {
        ttsPresenter.onStart(this)
        downloadCompleter?.let {
            downloadScheduler = DownloadCompletionScheduler(downloadCompleter!!, responseSource)
            downloadScheduler?.startDownloads()
        }
        articleStateSource = JSoupArticleStateSource(responseSource)
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

    override suspend fun onArticleFinished(articleState: ArticleState) {
        if (autoPlay) {
            autoPlayNext(articleState)
        }
        if (autoDelete) {
            autoDeleteCurrent(articleState)
        }
    }

    private fun autoDeleteCurrent(articleState: ArticleState) {
        responseSource.delete(articleState.title)
    }

    private suspend fun autoPlayNext(articleState: ArticleState) {
        var nextArticle: ArticleContext? = null
        if (currentCourse == LECTOR_UNIVERSE) {
            nextArticle = responseSource.getNextArticle(articleState.title).await()
        } else {
            nextArticle = courseSource.getNextInCourse(currentCourse, articleState.title)
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
            responseSource.updatePosition(currentRequestContext, it)
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
        if (responseSource.contains(urlToContext(urlString)).await()) {
                responseSource.getArticleContext(urlToContext(urlString))
                        .await()?.let{
                            position = it.position
                        }
        }
        articleStateSource?.getArticle(urlString)?.let {
            ttsPresenter.onArticleChanged(fastForward(it, position))
            if (it.title != currentRequestContext) {
                val previousTitle = currentRequestContext
                synchronized(currentRequestContext) {
                    currentRequestContext = it.title
                }
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
            if (!this@MainPresenter.responseSource.contains(computedContext).await()) {
                responseSource.add(computedContext).await()
            }
        }
    }

    override fun onRequest(url: String): Deferred<Response?> {
        var curContext: String? = null
        synchronized(currentRequestContext) {
            curContext = currentRequestContext
        }
        return responseSource.getWithContext(Request.Builder()
                .url(url)
                .build(), curContext!!)
    }

    override suspend fun saveArticle() {
        synchronized(currentRequestContext) {
            responseSource.markPermanent(currentRequestContext)
        }
    }

    override suspend fun courseDetailsRequested(courseContext: CourseContext) {
        courseContext.id?.let {
            currentCourse = courseContext.courseName
                courseSource.getArticlesForCourse(it)?.let {
                    displayArticleList(it.await(),
                            courseContext.courseName)
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

    override suspend fun onDisplayReadingList() {
        responseSource.getAllPermanent()?.let {
            displayArticleList(it.await(), ALL_ARTICLES)
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
            courseList.addAll(it.await())
        }
        view.onCoursesChanged()
        view.displayCourses()
    }

//    fun addCourse(courseDescription: CourseDescription) {
//        runBlocking {
//            val courseId = courseSource.add(ConcreteCourseContext(null,
//                    courseDescription.courseName,
//                    0)).await()
//            courseDescription.articleUrls.map {
//                async {
//                    responseSource.add(urlToContext(it)).await()
//                    courseSource.addArticleForSource(courseDescription.courseName,
//                            urlToContext(it)).await()
//                }
//            }.forEach {it.await() }
//        }
//    }

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

    override fun playAllPressed(title: String) {
        // todo: we are going to have to handle the notion of autoplay being temporarily enabled,
        // todo: or else change the language from "play all" to "start playing" or something else
        if (readingList != null && readingList.size >0 ) {
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
}
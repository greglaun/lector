package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainPresenter(val view : MainContract.View,
                    val store: Store)
    : MainContract.Presenter, StateHandler {

    override val readingList = mutableListOf<ArticleContext>()
    override val courseList = mutableListOf<CourseContext>()

    private var isActivityRunning = false

    override fun onAttach() {
        // todo(unidirectional): presenter should not have references to these
        store.stateHandlers.add(this)

        isActivityRunning = true
        handleState(store.state)
        GlobalScope.launch {
            store.dispatch(ReadAction.StartDownloadAction)
        }
    }

    override fun onDetach() {
        runBlocking {
            store.dispatch(ReadAction.StopDownloadAction)
        }
        isActivityRunning = false
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
                view.navigateBrowseCourses()
            }
            Navigation.MY_READING_LIST -> {
                val lce = state.readingListScreen.articles
                when (lce) {
                    is Lce.Success -> displayReadingList(lce.data)
                    is Lce.Loading -> displayReadingList(emptyList())
                    is Lce.Error -> view.onError(lce.message)
                }
            }
            Navigation.MY_COURSE_LIST -> {
                val lce = state.courseBrowserScreen.availableCourses
                when (lce) {
                    is Lce.Success -> {
                        displayCourses(lce.data)
                    }
                    is Lce.Loading -> displayCourses(emptyList())
                    is Lce.Error -> view.onError(lce.message)
                }
            }
        }
    }

    private fun handleCurrentArticle(state: State) {
        if (state.currentArticleScreen.newArticle) {
            handleNewArticle(state)
            return
        }
        view.unhideWebView()
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
        view.loadUrl(contextToUrl(state.currentArticleScreen.articleState.title)) {
            GlobalScope.launch {
                store.dispatch(UpdateAction.UpdateArticleFreshnessAction(
                        state.currentArticleScreen.articleState as ArticleState))
                store.dispatch(UpdateAction.UpdateNavigationAction(Navigation.CURRENT_ARTICLE))
            }
        }
    }

    override fun getLectorView(): MainContract.View? {
        return view
    }

    override suspend fun maybeGoToPreviousArticle() {
        store.dispatch(UpdateAction.MaybeGoBack)
    }

    override suspend fun loadFromContext(articleContext: ArticleContext) {
        store.dispatch(ReadAction.LoadNewUrlAction(contextToUrl(articleContext.contextString)))
    }

    override suspend fun saveArticle() {
        GlobalScope.launch{
            store.dispatch(WriteAction.SaveArticle(store.state.currentArticleScreen.articleState))
        }
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
                        readingList.remove(articleContext)
                        view.onReadingListChanged()
                        GlobalScope.launch {
                            store.dispatch(WriteAction.DeleteArticle(articleContext))
                        }
                    }
                })
    }

    override fun deleteRequested(courseContext: CourseContext) {
        view.confirmMessage("Delete course ${courseContext.courseName}?",
                onConfirmed = {
                    if (it) {
                        courseList.remove(courseContext)
                        view.onCoursesChanged()
                        GlobalScope.launch {
                            store.dispatch(WriteAction.DeleteCourse(courseContext))
                        }
                    }
                })
    }

    override suspend fun onDisplayReadingList() {
        store.dispatch(ReadAction.FetchAllPermanentAndDisplay)
    }

    private fun displayReadingList(articleList: List<ArticleContext>, title: String? = null) {
        readingList.clear()
        readingList.addAll(articleList)
        view.onReadingListChanged()
        view.displayReadingList(title)
    }

    private fun displayCourses(courses: List<CourseContext>) {
        courseList.clear()
        courseList.addAll(courses)
        view.onCoursesChanged()
        view.displayCourses()
    }

    override suspend fun onDisplaySavedCourses() {
        store.dispatch(ReadAction.FetchAllCoursesAndDisplay)
    }

    override suspend fun onBrowseCourses() {
        store.dispatch(ReadAction.FetchAllCoursesAndDisplay)
    }


    override fun evaluateJavascript(js: String, callback: ((String) -> Unit)?) {
        view.evaluateJavascript(js, callback)
    }

    override fun playAllPressed(title: String) {
        if (readingList.size > 0) {
            GlobalScope.launch {
                store.dispatch( ReadAction.LoadNewUrlAction(
                        contextToUrl(readingList[0].contextString)))
                store.dispatch(SpeakerAction.SpeakAction)
            }
        }
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        GlobalScope.launch {
            store.dispatch(PreferenceAction.SetAutoPlay(autoPlay))
        }
    }

    override fun setAutoDelete(autoDelete: Boolean) {
        GlobalScope.launch {
            store.dispatch(PreferenceAction.SetAutoPlay(autoDelete))
        }
    }

}
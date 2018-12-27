package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import com.greglaun.lector.ui.speak.ArticleState
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Response

interface MainContract {
    interface View : LectorView {
        fun loadUrl(urlString : String)
        fun enablePlayButton()
        fun enablePauseButton()
        fun displayReadingList()
        fun highlightText(articleState: ArticleState,
                          onDone: ((ArticleState, String) -> Unit)? = null)
        fun unhighlightAllText()
        fun unhideWebView()
        fun unHideReadingListView()
        fun unHideCourseListView()
        fun onReadingListChanged()
        fun onCoursesChanged()
        fun displayCourses()
        fun evaluateJavascript(js: String, callback: ((String) -> Unit)?)
    }

    interface Presenter : LectorPresenter<View> {
        val readingList: MutableList<ArticleContext>
        val courseList: MutableList<CourseContext>
        fun onPlayButtonPressed()
        fun stopSpeakingAndEnablePlayButton()
        fun saveArticle()
        fun deleteRequested(articleContext: ArticleContext)
        fun onUrlChanged(url : String)
        fun onRequest(url : String) : Deferred<Response?>
        fun onDisplayReadingList()
        fun onRewindOne()
        fun onForwardOne()
        fun responseSource(): ResponseSource
        fun courseSource(): CourseSource
        fun loadFromContext(articleContext: ArticleContext)
        fun onDisplayCourses()
        fun deleteRequested(courseContext: CourseContext)
        fun courseDetailsRequested(courseContext: CourseContext)
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun setSpeechRate(speechRate: Float)
        fun evaluateJavascript(js: String, callback: ((String) -> Unit)?)
    }
}
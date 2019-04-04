package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.net.DownloadCompleter
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import com.greglaun.lector.ui.speak.ArticleState
import okhttp3.Response

interface MainContract {
    interface View : LectorView {
        fun loadUrl(urlString : String, onLoaded: (suspend (String) -> Unit)?)
        fun enablePlayButton()
        fun enablePauseButton()
        fun displayReadingList(title: String? = null)
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

        fun navigateBrowseCourses()
    }

    interface Presenter : LectorPresenter<View> {
        val readingList: MutableList<ArticleContext>
        val courseList: MutableList<CourseContext>

        fun onPlayButtonPressed()
        fun stopSpeakingAndEnablePlayButton()
        suspend fun saveArticle()
        fun deleteRequested(articleContext: ArticleContext)
        suspend fun onDisplayReadingList()
        suspend fun onRewindOne()
        suspend fun onForwardOne()
        suspend fun loadFromContext(articleContext: ArticleContext)
        suspend fun onDisplaySavedCourses()
        suspend fun onBrowseCourses()
        fun deleteRequested(courseContext: CourseContext)
        suspend fun courseDetailsRequested(courseContext: CourseContext)
        fun setHandsomeBritish(shouldBeBritish: Boolean)
        fun evaluateJavascript(js: String, callback: ((String) -> Unit)?)
        suspend fun maybeGoBack()

        // Preferences
        fun setSpeechRate(speechRate: Float)
        fun setAutoPlay(autoPlay: Boolean)
        fun setAutoDelete(autoDelete: Boolean)
        fun playAllPressed(title: String)
    }
}
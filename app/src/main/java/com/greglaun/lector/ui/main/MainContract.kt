package com.greglaun.lector.ui.main

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView
import com.greglaun.lector.ui.speak.ArticleState

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

        suspend fun saveArticle()
        fun deleteRequested(articleContext: ArticleContext)
        suspend fun onDisplayReadingList()
        suspend fun loadFromContext(articleContext: ArticleContext)
        suspend fun onDisplaySavedCourses()
        suspend fun onBrowseCourses()
        fun deleteRequested(courseContext: CourseContext)
        suspend fun courseDetailsRequested(courseContext: CourseContext)
        suspend fun maybeGoBack()
        fun evaluateJavascript(js: String, callback: ((String) -> Unit)?)

        // Preferences
        fun setAutoPlay(autoPlay: Boolean)
        fun setAutoDelete(autoDelete: Boolean)
        fun playAllPressed(title: String)
    }
}
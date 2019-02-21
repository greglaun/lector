package com.greglaun.lector.store

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.course.CourseContext

private val DEFAULT_PAGE = "MAIN_PAGE"
private val DEFAULT_READING_LIST = "All Articles"

enum class Navigation {
    CURRENT_ARTICLE,
    NEW_ARTICLE,
    MY_READING_LIST,
    MY_COURSE_LIST,
    BROWSE_COURSES
}

data class CurrentArticleScreen(val currentContext: String = DEFAULT_PAGE)
data class ReadingListScreen(val currentReadingList: String = DEFAULT_READING_LIST,
                             val articles: List<ArticleContext> = emptyList())
data class CourseBrowserScreen(val availableCourses: List<CourseContext> = emptyList())

data class State(
        val currentArticleScreen: CurrentArticleScreen = CurrentArticleScreen(),
        val readingListScreen: ReadingListScreen = ReadingListScreen(),
        val courseBrowserScreen: CourseBrowserScreen = CourseBrowserScreen(),
        val navigation: Navigation = Navigation.CURRENT_ARTICLE
)
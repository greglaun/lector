package com.greglaun.lector.store

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.ui.speak.ArticlePosition
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.SpeakerState

val DEFAULT_PAGE = "MAIN_PAGE"
val DEFAULT_READING_LIST = "All Articles"
val LECTOR_UNIVERSE = ""

enum class Navigation {
    CURRENT_ARTICLE,
    NEW_ARTICLE,
    MY_READING_LIST,
    MY_COURSE_LIST,
    BROWSE_COURSES
}

enum class Changed {
    NONE,
    PREFERENCE,
}

data class CurrentArticleScreen(val currentContext: String = DEFAULT_PAGE,
                                val currentCourse: String = LECTOR_UNIVERSE,
                                val articleState: ArticleState = ArticleState(currentContext,
                                        listOf(""), ArticlePosition(0, "")),
                                val speakerState: SpeakerState = SpeakerState.NOT_READY)

data class ReadingListScreen(val currentReadingList: String = DEFAULT_READING_LIST,
                             val articles: List<ArticleContext> = emptyList())
data class CourseBrowserScreen(val availableCourses: List<CourseContext> = emptyList())

data class Preferences(val autoPlay: Boolean = true,
                       val autoDelete: Boolean = true,
                       val isBritish: Boolean = false,
                       val isSlow: Boolean = false,
                       val speechRate: Float = 1f)

data class State(
        val currentArticleScreen: CurrentArticleScreen = CurrentArticleScreen(),
        val readingListScreen: ReadingListScreen = ReadingListScreen(),
        val courseBrowserScreen: CourseBrowserScreen = CourseBrowserScreen(),
        val navigation: Navigation = Navigation.CURRENT_ARTICLE,
        val changed: List<Changed> = listOf(Changed.NONE)
)

fun State.updateArticleScreen(newArticleScreen: CurrentArticleScreen): State {
    return State(newArticleScreen, readingListScreen, courseBrowserScreen, navigation, changed)
}
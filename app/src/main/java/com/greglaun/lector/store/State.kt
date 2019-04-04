package com.greglaun.lector.store

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.EmptyCourseContext
import com.greglaun.lector.ui.speak.AbstractArticleState
import com.greglaun.lector.ui.speak.EmptyArticleState
import java.util.*

val DEFAULT_READING_LIST = "All Articles"
val LECTOR_UNIVERSE = ""

enum class Navigation {
    CURRENT_ARTICLE,
    MY_READING_LIST,
    MY_COURSE_LIST,
    BROWSE_COURSES
}

enum class Changed {
    NONE,
    PREFERENCE,
    BACKGROUND
}

enum class SpeakerState {
    NOT_READY,
    READY,
    SPEAKING,
    SPEAKING_NEW_UTTERANCE
}

data class CurrentArticleScreen(val articleState: AbstractArticleState = EmptyArticleState,
                                val currentCourse: CourseContext = EmptyCourseContext(),
                                val newArticle: Boolean = false)

data class ReadingListScreen(val currentReadingList: String = DEFAULT_READING_LIST,
                             val articles: Lce<List<ArticleContext>> =
                                     Lce.Success(emptyList()))
data class CourseBrowserScreen(val availableCourses: Lce<List<CourseContext>> =
                                       Lce.Success(emptyList()))

data class Preferences(val autoPlay: Boolean = true,
                       val autoDelete: Boolean = true,
                       val isBritish: Boolean = false,
                       val isSlow: Boolean = false,
                       val speechRate: Float = 1f)

data class Background(val downloadFinisher: Boolean = false)

data class State(
        val currentArticleScreen: CurrentArticleScreen = CurrentArticleScreen(
                EmptyArticleState,
                EmptyCourseContext(),
                true),
        val readingListScreen: ReadingListScreen = ReadingListScreen(),
        val courseBrowserScreen: CourseBrowserScreen = CourseBrowserScreen(),
        val navigation: Navigation = Navigation.CURRENT_ARTICLE,
        val preferences: Preferences = Preferences(),
        val background: Background = Background(),
        val changed: List<Changed> = listOf(Changed.NONE),
        val speakerState: SpeakerState = SpeakerState.NOT_READY,
        val preferenceChanged: Boolean = false
)

fun State.updateArticleScreen(newArticleScreen: CurrentArticleScreen,
                              speakerState: SpeakerState): State {
    return State(newArticleScreen, readingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, speakerState)
}

fun State.newArticleScreen(newArticleScreen: CurrentArticleScreen,
                              speakerState: SpeakerState): State {
    return State(newArticleScreen, readingListScreen, courseBrowserScreen,
            navigation,
            preferences, background, changed, speakerState)
}

fun State.updateNavigation(navigation: Navigation): State {
    return State(currentArticleScreen, readingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, stripNew(speakerState))
}

fun State.updateReadingListScreen(newReadingListScreen: ReadingListScreen): State {
    return State(currentArticleScreen, newReadingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, stripNew(speakerState))
}

fun State.updateCourseBrowserScreen(courseBrowserScreen: CourseBrowserScreen): State {
    return State(currentArticleScreen, readingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, stripNew(speakerState))
}

fun State.updateSpeakerState(speakerState: SpeakerState): State {
    return State(currentArticleScreen, readingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, speakerState)
}

fun stripNew(speakerState: SpeakerState): SpeakerState {
    when (speakerState) {
        SpeakerState.SPEAKING_NEW_UTTERANCE -> return SpeakerState.SPEAKING
        else -> return speakerState
    }
}

fun maybeNew(speakerState: SpeakerState): SpeakerState {
    when (speakerState) {
        SpeakerState.SPEAKING -> return SpeakerState.SPEAKING_NEW_UTTERANCE
        else -> return speakerState
    }
}

fun State.updatePreferences(preferences: Preferences): State {
    return State(currentArticleScreen, readingListScreen, courseBrowserScreen, navigation,
            preferences, background, changed, speakerState, preferenceChanged = true)
}

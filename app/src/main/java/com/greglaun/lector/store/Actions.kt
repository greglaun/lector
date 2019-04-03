package com.greglaun.lector.store

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.ThinCourseDetails
import com.greglaun.lector.ui.speak.AbstractArticleState
import com.greglaun.lector.ui.speak.ArticleState

sealed class Action

sealed class UpdateAction: Action() {
    data class UpdateArticleAction(val articleState: AbstractArticleState): UpdateAction()
    data class NewArticleAction(val articleState: AbstractArticleState): UpdateAction()
    data class UpdateNavigationAction(val navigation: Navigation): UpdateAction()
    data class UpdateCourseDetailsAction(val courseDetails: ThinCourseDetails): UpdateAction()
    data class UpdateSpeakerStateAction(val speakerState: SpeakerState): UpdateAction()
    data class UpdateArticleFreshnessAction(val articleState: ArticleState,
                                            val isNew: Boolean = false): UpdateAction()
    data class UpdateReadingListAction(val readingListLce: Lce<List<ArticleContext>>):
            UpdateAction()
    data class UpdateCourseBrowseList(val courseListLce: Lce<List<CourseContext>>): UpdateAction()
    data class UpdateArticlesForCourse(val courseArticlesLce: Lce<List<ArticleContext>>): UpdateAction()
    class FastForwardOne: UpdateAction()
    class RewindOne: UpdateAction()
    class ArticleOverAction: UpdateAction()
}

sealed class SpeakerAction: Action() {
    class StopSpeakingAction: SpeakerAction()
    class SpeakAction: SpeakerAction()
}

sealed class ReadAction: Action() {
    data class FetchCourseDetailsAction(val courseContext: CourseContext) : ReadAction()
    class StartDownloadAction: ReadAction()
    class StopDownloadAction: ReadAction()
    data class LoadNewUrlAction(val newUrl: String): ReadAction()
    class FetchAllPermanentAndDisplay : ReadAction()
    data class FetchArticlesForCourseAndDisplay(var courseContext: CourseContext): ReadAction()
    class FetchAllCoursesAndDisplay: ReadAction()
}

sealed class WriteAction: Action() {
    data class SaveArticle(val articleState: AbstractArticleState): WriteAction()
    data class DeleteArticle(val articleContext: ArticleContext): WriteAction()
    data class DeleteCourse(val courseContext: CourseContext): WriteAction()
    data class MarkDownloadFinished(val urlString: String): WriteAction()
}

sealed class PreferenceAction: Action() {
    data class SetHandsomeBritish(val shouldBeBritish: Boolean): PreferenceAction()
    data class SetSpeechRate(val speechRate: Float): PreferenceAction()
    data class SetAutoPlay(val isAutoPlay: Boolean): PreferenceAction()
    data class SetAutoDelete(val isAutoDelete: Boolean): PreferenceAction()
    data class SetIsSlow(val isSlow: Boolean): PreferenceAction()
}
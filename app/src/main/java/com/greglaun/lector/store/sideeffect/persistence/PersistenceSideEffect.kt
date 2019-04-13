package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleStateSource
import com.greglaun.lector.ui.speak.JSoupArticleStateSource
import java.util.*

class PersistenceSideEffect(val store: Store, val responseSource: ResponseSource,
                            private val courseSource: CourseSource,
                            private val courseDownloader: CourseDownloader,
                            private val articleStateSource:
                            ArticleStateSource = JSoupArticleStateSource(
                              responseSource)): SideEffect {


    private val sessionHistory: Stack<String> = Stack()

    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.FetchCourseDetailsAction ->
                handleFetchCourseDetails(action, courseDownloader) { store.dispatch(it) }
            is ReadAction.LoadNewUrlAction -> loadNewUrl(
                    action,
                    responseSource,
                    articleStateSource, sessionHistory)  {
                store.dispatch(it)
            }
            is ReadAction.FetchAllPermanentAndDisplay ->
                handleFetchAllPermanentAndDisplay(responseSource) {
                    store.dispatch(it)
                }
            is ReadAction.FetchAllCoursesAndDisplay -> handleFetchAllCoursesAndDisplay(
                    courseSource) {
                store.dispatch(it)
            }
            is ReadAction.FetchArticlesForCourseAndDisplay ->
                handleFetchArticlesForCourseAndDisplay(action, courseSource) {
                    store.dispatch(it)
                }
            is ReadAction.FetchSavedCoursesAndDisplay -> handleFetchSavedCourses(action,
                    courseSource) {
                store.dispatch(it)
            }
            is UpdateAction.ArticleOverAction -> handleArticleOver(
                    store.state,
                    responseSource,
                    courseSource,
                    articleStateSource) {
                store.dispatch(it)
            }
            is UpdateAction.MaybeGoBack -> handleMaybeGoBack(sessionHistory) {
                store.dispatch(it)
            }
            is WriteAction.SaveArticle -> handleSaveArticle(action, responseSource)
            is WriteAction.DeleteArticle -> handleDeleteArticle(action, responseSource)
            is WriteAction.DeleteCourse -> handleDeleteCourse(action, courseSource)
            is WriteAction.MarkDownloadFinished -> handleMarkDownloadFinished(action,
                    responseSource)
        }
    }

}

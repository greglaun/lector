package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleStateSource
import com.greglaun.lector.ui.speak.JSoupArticleStateSource
import java.util.*

class PersistenceSideEffect(val store: Store, val responseSource: ResponseSource,
                            val courseSource: CourseSource,
                            val courseDownloader: CourseDownloader,
                            val articleStateSource: ArticleStateSource = JSoupArticleStateSource(
                              responseSource)): SideEffect {


    val session_history: Stack<String> = Stack()

    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.FetchCourseDetailsAction ->
                handleFetchCourseDetails(action, courseDownloader) { store.dispatch(it) }
            is ReadAction.LoadNewUrlAction -> loadNewUrl(
                    action,
                    responseSource,
                    articleStateSource, session_history)  {
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
            is UpdateAction.ArticleOverAction -> handleArticleOver(
                    store.state,
                    responseSource,
                    courseSource,
                    articleStateSource) {
                store.dispatch(it)
            }
            is UpdateAction.MaybeGoBack -> handleMaybeGoBack(session_history) {
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

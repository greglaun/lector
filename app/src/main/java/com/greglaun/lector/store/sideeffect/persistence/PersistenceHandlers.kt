package com.greglaun.lector.store.sideeffect.persistence

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.EmptyCourseContext
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleStateSource
import java.util.*

suspend fun handleFetchCourseDetails(action: ReadAction.FetchCourseDetailsAction,
                                     courseDownloader: CourseDownloader,
                                     actionDispatcher: suspend (Action) -> Unit) {
    val courseName = action.courseContext.courseName
    val detailsMap = courseDownloader.fetchCourseDetails(listOf(courseName))
    detailsMap?.let {
        if (detailsMap.containsKey(courseName)) {
            val details = detailsMap[courseName]
            details?.let {
                actionDispatcher(UpdateAction.UpdateCourseDetailsAction(details))
            }
        }
    }
}

suspend fun loadNewUrl(action: ReadAction.LoadNewUrlAction,
                       responseSource: ResponseSource,
                       articleStateSource: ArticleStateSource,
                       history: Stack<String>,
                       actionDispatcher: suspend (Action) -> Unit) {
    val title = urlToContext(action.newUrl)
    if (!responseSource.contains(title)) {
        responseSource.add(title)
    }
    responseSource.getArticleContext(title)?.let {articleContext ->
        articleStateSource.getArticle(articleContext)?.let {articleState ->
            if (action.addToHistory) {
                history.push(title)
            }
            actionDispatcher(UpdateAction.NewArticleAction(articleState))
        }
    }
}

suspend fun handleArticleOver(state: State,
                              responseSource: ResponseSource,
                              courseSource: CourseSource,
                              articleStateSource: ArticleStateSource,
                              actionDispatcher: suspend (Action) -> Unit) {
    if (state.preferences.autoPlay) {
        autoPlayNext(state, responseSource, courseSource, articleStateSource, actionDispatcher)
        actionDispatcher(SpeakerAction.SpeakAction)
    }

    if (state.preferences.autoDelete) {
        responseSource.delete(state.currentArticleScreen.articleState.title)
    }
}

private suspend fun autoPlayNext(state: State, responseSource: ResponseSource,
                                 courseSource: CourseSource,
                                 articleStateSource: ArticleStateSource,
                                 actionDispatcher: suspend (Action) -> Unit) {
    val nextArticle: ArticleContext?
    val currentArticle = state.currentArticleScreen.articleState
    val currentCourse = state.currentArticleScreen.currentCourse
    nextArticle = if (currentCourse == EmptyCourseContext()) {  // Not in a course
        responseSource.getNextArticle(currentArticle.title)
    } else {
        courseSource.getNextInCourse(
                currentCourse.courseName, currentArticle.title)
    }
    if (nextArticle == null) {
        actionDispatcher(SpeakerAction.StopSpeakingAction)
        return
    }
    val nextArticleState = articleStateSource.getArticle(nextArticle)
    if (nextArticleState == null) {
        actionDispatcher(SpeakerAction.StopSpeakingAction)
        return
    }
    actionDispatcher.invoke(UpdateAction.NewArticleAction(nextArticleState))
}

suspend fun handleFetchAllPermanentAndDisplay(responseSource: ResponseSource,
                                              actionDispatcher: suspend (Action) -> Unit) {
    // todo(i18n): Better handling of error strings.
    var readingListLce: Lce<List<ArticleContext>> = Lce.Error("Unable to download reading list.")
    responseSource.getAllPermanent()?.let {
        readingListLce = Lce.Success(it)
    }
    actionDispatcher.invoke(UpdateAction.UpdateReadingListAction(readingListLce = readingListLce))
}

suspend fun handleFetchAllCoursesAndDisplay(courseSource: CourseSource,
                                            actionDispatcher: suspend (Action) -> Unit) {
    // todo(i18n): Better handling of error strings.
    var courseListLce: Lce<List<CourseContext>> = Lce.Error("Unable to download courses.")
    courseSource.getCourses()?.let {
        courseListLce = Lce.Success(it)
    }
    actionDispatcher.invoke(UpdateAction.UpdateCourseBrowseList(courseListLce))
}

suspend fun handleFetchArticlesForCourseAndDisplay(
        action: ReadAction.FetchArticlesForCourseAndDisplay,
        courseSource: CourseSource,
        actionDispatcher: suspend (Action) -> Unit) {
    action.courseContext.id?.let {
        // todo(i18n): Better handling of error strings.
        var courseArticlesList: Lce<List<ArticleContext>> = Lce.Error("Unable to download reading list.")
        courseSource.getArticlesForCourse(it)?.let {articleList ->
            courseArticlesList = Lce.Success(articleList)
        }
        actionDispatcher.invoke(UpdateAction.UpdateArticlesForCourse(courseArticlesList))
    }
}

suspend fun handleSaveArticle(action: WriteAction.SaveArticle,
                              responseSource: ResponseSource) {
        val title = action.articleState.title
        responseSource.markPermanent(title)
}

suspend fun handleDeleteArticle(action: WriteAction.DeleteArticle,
                                responseSource: ResponseSource) {
    responseSource.delete(action.articleContext.contextString)
}

suspend fun handleDeleteCourse(action: WriteAction.DeleteCourse,
                                courseSource: CourseSource) {
    courseSource.delete(action.courseContext.courseName)
}

suspend fun handleMarkDownloadFinished(action: WriteAction.MarkDownloadFinished,
                                       responseSource: ResponseSource) {
    responseSource.markFinished(urlToContext(action.urlString))
}

suspend fun handleMaybeGoBack(history: Stack<String>, actionDispatcher: suspend (Action) -> Unit) {
    if (history.size < 2) {
        return
    }
    history.pop()
    val previous = history.pop()
    actionDispatcher.invoke(ReadAction.LoadNewUrlAction(previous, false))
}

suspend fun handleFetchSavedCourses(action: ReadAction.FetchSavedCoursesAndDisplay,
                                    courseSource: CourseSource,
                                    actionDispatcher: suspend (Action) -> Unit) {
    courseSource.getCourses()?.let {courses ->
        actionDispatcher.invoke(UpdateAction.UpdateSavedCoursesAction(
                Lce.Success(courses)))
    }
}

suspend fun handleNewPosition(store: Store, responseSource: ResponseSource) {
    // If we are here, the reduces have already updated the article state, so the updated position
    // is the current position.
    val articleState = store.state.currentArticleScreen.articleState
    articleState.currentPosition
    responseSource.updatePosition(articleState.title, articleState.currentPosition.utteranceId)
}
package com.greglaun.lector.store.sideeffect.fetch

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.contextToUrl
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.EmptyCourseContext
import com.greglaun.lector.store.*
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.ArticleStateSource

suspend fun fetchCourseDetails(action: ReadAction.FetchCourseDetailsAction,
                               courseDownloader: CourseDownloader,
                               actionDispatcher: suspend (Action) -> Unit) {
    val courseName = action.courseContext.courseName
    val detailsMap = courseDownloader.fetchCourseDetails(listOf(courseName))
    detailsMap?.let {
        if (detailsMap.containsKey(courseName)) {
            val details = detailsMap.get(courseName)
            details?.let {
                actionDispatcher(UpdateAction.UpdateCourseDetailsAction(details))
            }
        }
    }
}

suspend fun loadNewUrl(action: ReadAction.LoadNewUrlAction,
                       responseSource: ResponseSource,
                       articleStateSource: ArticleStateSource,
                       actionDispatcher: suspend (Action) -> Unit) {
    articleStateSource.getArticle(action.newUrl)?.also {
        if (!responseSource.contains(it.title)) {
            responseSource.add(it.title)
        }
        actionDispatcher(UpdateAction.NewArticleAction(it))
    }
}

suspend fun handleArticleOver(action: UpdateAction.ArticleOverAction,
                              store: Store,
                              responseSource: ResponseSource,
                              courseSource: CourseSource,
                              articleStateSource: ArticleStateSource,
                              actionDispatcher: suspend (Action) -> Unit) {
    actionDispatcher(SpeakerAction.StopSpeakingAction())
    if (store.state.preferences.autoPlay) {
        autoPlayNext(store, responseSource, courseSource, actionDispatcher, articleStateSource)
        actionDispatcher(SpeakerAction.SpeakAction())
    }

    if (store.state.preferences.autoDelete) {
        responseSource.delete(store.state.currentArticleScreen.articleState.title)
    }
}

private suspend fun autoPlayNext(store: Store, responseSource: ResponseSource,
                                 courseSource: CourseSource,
                                 actionDispatcher: suspend (Action) -> Unit,
                                 articleStateSource: ArticleStateSource) {
    val nextArticle: ArticleContext?
    val currentArticle = store.state.currentArticleScreen.articleState
    val currentCourse = store.state.currentArticleScreen.currentCourse
    if (currentCourse == EmptyCourseContext()) {// Not in a course
        nextArticle = responseSource.getNextArticle(currentArticle.title)
    } else {
        nextArticle = courseSource.getNextInCourse(
                currentCourse.courseName, currentArticle.title)
    }
    if (nextArticle == null) {
        actionDispatcher(SpeakerAction.StopSpeakingAction())
        return
    }
    val nextArticleState = articleStateSource.getArticle(nextArticle)
    if (nextArticleState == null) {
        actionDispatcher(SpeakerAction.StopSpeakingAction())
        return

    }
    store.dispatch(UpdateAction.NewArticleAction(nextArticleState))
}


//fun computeNewArticle(newUrl: String): ArticleState? {
//    if (!newUrl.contains("index.php?search=")) {
//        return articleStatefromTitle(urlToContext(newUrl))
//    }
//    if (newUrl.substringAfterLast("search=") == "") {
//        // newUrl is a search result for nothing.
//        return null
//    }
//    // Check for redirects
//    // todo(REST): do a REST call and get the redirect info from the API directly.
//    val client = OkHttpClient().newBuilder()
//            .followRedirects(false)
//            .followSslRedirects(false)
//            .build()
//    val request = Request.Builder()
//            .url(newUrl)
//            .build()
//    val response = client.newCall(request).execute() ?: return null
//    if (response.isRedirect) {
//        val url = response.networkResponse()?.headers()?.toMultimap()?.
//                get("Location") ?: return null
//        return articleStatefromTitle(urlToContext(url.get(0)))
//    }
//    return articleStatefromTitle(urlToContext(newUrl))
//}
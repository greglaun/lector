package com.greglaun.lector.store.sideeffect.fetch

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.UpdateAction
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.ArticleStateSource
import com.greglaun.lector.ui.speak.articleStatefromTitle
import okhttp3.OkHttpClient
import okhttp3.Request

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
        actionDispatcher(UpdateAction.UpdateArticleAction(it))
    }
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
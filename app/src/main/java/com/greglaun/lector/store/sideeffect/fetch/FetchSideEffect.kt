package com.greglaun.lector.store.sideeffect.fetch

import android.util.Log
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.SideEffect
import com.greglaun.lector.store.Store
import com.greglaun.lector.ui.speak.ArticleState
import com.greglaun.lector.ui.speak.ArticleStateSource
import com.greglaun.lector.ui.speak.JSoupArticleStateSource
import com.greglaun.lector.ui.speak.articleStatefromTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class FetchSideEffect(val store: Store, val responseSource: ResponseSource,
                      val courseSource: CourseSource,
                      val courseDownloader: CourseDownloader,
                      val articleStateSource: ArticleStateSource = JSoupArticleStateSource(
                              responseSource)): SideEffect {


    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.FetchCourseDetailsAction ->
                fetchCourseDetails(action, courseDownloader) { store.dispatch(it) }
            is ReadAction.LoadNewUrlAction -> loadNewUrl(
                    action,
                    responseSource,
                    articleStateSource)  {
                store.dispatch(it)
            }
        }
    }
}

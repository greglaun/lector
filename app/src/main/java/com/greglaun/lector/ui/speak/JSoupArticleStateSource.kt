package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.net.TEN_GB
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.Request

class JSoupArticleStateSource(val responseSource: ResponseSource) : ArticleStateSource {
    override fun getArticle(urlString: String): Deferred<ArticleState?> {
        return GlobalScope.async {
            val cacheRequest = Request.Builder()
                    .url(urlString)
                    .build()
            val html = responseSource.getWithContext(cacheRequest, urlToContext(urlString)).await()
            jsoupStateFromHtml(html!!.peekBody(TEN_GB).string())
        }
    }
}
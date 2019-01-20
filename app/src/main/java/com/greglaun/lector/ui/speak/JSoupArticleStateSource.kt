package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.net.TEN_GB
import okhttp3.Request

class JSoupArticleStateSource(val responseSource: ResponseSource) : ArticleStateSource {
    override suspend fun getArticle(urlString: String): ArticleState? {
        val cacheRequest = Request.Builder()
                .url(urlString)
                .build()
        val html = responseSource.getWithContext(cacheRequest, urlToContext(urlString)).await()
        return jsoupStateFromHtml(html!!.peekBody(TEN_GB).string())
    }
}
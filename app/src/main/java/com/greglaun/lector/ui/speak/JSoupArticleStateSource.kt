package com.greglaun.lector.ui.speak

import com.greglaun.lector.android.room.ArticleCacheDatabase
import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.toResponse
import com.greglaun.lector.data.net.TEN_GB

class JSoupArticleStateSource(val articleCacheDatabase: ArticleCacheDatabase) : ArticleStateSource {
    override fun getArticle(urlString: String): ArticleState {
        val html = articleCacheDatabase.cachedResponseDao().get(urlString.md5())
        val response = html.response.toResponse()
        return jsoupStateFromHtml(response.peekBody(TEN_GB).string())
    }
}
package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.ArticleContext

interface ArticleStateSource {
    suspend fun getArticle(urlString: String): ArticleState?
    suspend fun getArticle(articleContext: ArticleContext): ArticleState?
}

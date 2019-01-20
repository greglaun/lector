package com.greglaun.lector.ui.speak

interface ArticleStateSource {
    suspend fun getArticle(urlString: String): ArticleState?
}

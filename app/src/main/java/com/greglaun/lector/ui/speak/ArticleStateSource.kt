package com.greglaun.lector.ui.speak

interface ArticleStateSource {
    fun getArticle(urlString: String): ArticleState
}

package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.Deferred

interface ArticleStateSource {
    fun getArticle(urlString: String): Deferred<ArticleState?>
}

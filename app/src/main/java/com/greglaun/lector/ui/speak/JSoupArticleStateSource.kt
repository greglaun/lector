package com.greglaun.lector.ui.speak

class JSoupArticleStateSource : ArticleStateSource {
    override fun getArticle(urlString: String): ArticleState {
        return jsoupStateFromUrl(urlString)
    }
}
package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.ArticleContext

fun addIfAbsent(keyContext: String, db: LectorDatabase): ArticleContext? {
    var articleContext: ArticleContext?
    articleContext = db.articleContextDao().get(keyContext)
    if (articleContext == null) {
        val articleId = db.articleContextDao().insert(
                RoomArticleContext(null, keyContext, "", true))
        if (articleId != null) {
            articleContext = RoomArticleContext(articleId, keyContext, "", true)
        }
    }
    return articleContext
}

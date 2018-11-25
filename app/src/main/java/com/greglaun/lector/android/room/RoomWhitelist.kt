package com.greglaun.lector.android.room

import com.greglaun.lector.data.whitelist.Whitelist
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class RoomWhitelist(val db: ArticleCacheDatabase): Whitelist<String> {
    override fun contains(element: String): Deferred<Boolean> {
        return GlobalScope.async{
            db.articleContextDao().get(element) != null
        }
    }

    override fun add(element: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().insert(ArticleContext(null, contextString = element))
        }
    }

    override fun delete(element: String): Deferred<Unit> {
        return GlobalScope.async{
            db.articleContextDao().delete(element)
        }
    }

    override fun iterator(): Iterator<String> {
        return db.articleContextDao().getAll().map {
            it -> it.contextString }
                .iterator()
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        return GlobalScope.async {
            val articleContext = db.articleContextDao().get(from)
            articleContext.contextString = to
            db.articleContextDao().updateArticleContext(articleContext)
        }
    }

}
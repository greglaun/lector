package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class RoomCacheEntryClassifier(val db: LectorDatabase): CacheEntryClassifier<String> {
    override fun contains(element: String): Deferred<Boolean> {
        return GlobalScope.async{
            db.articleContextDao().get(element) != null
        }
    }

    override fun add(element: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().insert(RoomArticleContext(null, contextString = element))
            Unit
        }
    }

    override fun delete(element: String): Deferred<Unit> {
        return GlobalScope.async{
            db.articleContextDao().delete(element)
        }
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        return GlobalScope.async {
            val articleContext = db.articleContextDao().get(from)
            articleContext.contextString = to
            db.articleContextDao().updateArticleContext(articleContext)
        }
    }

    override fun getAllTemporary(): Deferred<List<ArticleContext>> {
        return GlobalScope.async {
            db.articleContextDao().getAllTemporary()
        }
    }

    override fun markTemporary(element: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().markTemporary(element)
        }
    }

    override fun markPermanent(element: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().markPermanent(element)
        }
    }

    override fun isTemporary(element: String): Deferred<Boolean> {
        return GlobalScope.async {
            db.articleContextDao().isTemporary(element)
        }
    }

    override fun getAllPermanent(): Deferred<List<ArticleContext>> {
        return GlobalScope.async {
            db.articleContextDao().getAllPermanent()
        }
    }

    override fun getArticleContext(context: String): Deferred<ArticleContext> {
        return GlobalScope.async {
            db.articleContextDao().get(context)
        }
    }

    override fun updatePosition(currentRequestContext: String, position: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().updatePosition(currentRequestContext, position)
        }
    }
}
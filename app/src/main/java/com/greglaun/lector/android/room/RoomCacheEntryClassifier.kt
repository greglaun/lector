package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class RoomCacheEntryClassifier(val db: LectorDatabase): CacheEntryClassifier<String> {
    override suspend fun contains(element: String): Boolean {
       return db.articleContextDao().get(element) != null
    }

    override suspend fun add(element: String): Long {
        val existingEntry = db.articleContextDao().get(element)
        if (existingEntry == null) {
            return db.articleContextDao().insert(RoomArticleContext(null, contextString = element))
        } else {
            return existingEntry.id!!
        }
    }

    override suspend fun delete(element: String) {
        return db.articleContextDao().delete(element)
    }

    override suspend fun update(from: String, to: String) {
        val articleContext = db.articleContextDao().get(from)
        articleContext?.let {
            articleContext.contextString = to
            db.articleContextDao().updateArticleContext(articleContext)
        }
    }

    override suspend fun getAllTemporary(): List<ArticleContext> {
        return db.articleContextDao().getAllTemporary()
    }

    override suspend fun markTemporary(element: String) {
        return db.articleContextDao().markTemporary(element)
    }

    override suspend fun markPermanent(element: String) {
        return db.articleContextDao().markPermanent(element)
    }

    override suspend fun isTemporary(element: String): Boolean {
        return db.articleContextDao().isTemporary(element)
    }

    override suspend  fun getAllPermanent(): List<ArticleContext> {
        return db.articleContextDao().getAllPermanent()
    }

    override suspend fun getArticleContext(context: String): ArticleContext? {
        return db.articleContextDao().get(context)
    }

    override suspend fun updatePosition(currentRequestContext: String, position: String) {
        return db.articleContextDao().updatePosition(currentRequestContext, position)
    }

    override fun getUnfinished(): Deferred<List<String>> {
        return GlobalScope.async {
            val unfinished = mutableListOf<String>()
            db.articleContextDao().getAllUnfinished().map {
                unfinished.add(it.contextString)
            }
            unfinished
        }
    }

    override fun markFinished(element: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().markFinished(element)
        }
    }

    override suspend fun getNextArticle(context: String): ArticleContext? {
        val oldArticle = db.articleContextDao().get(context) ?: return null
            if (oldArticle.temporary) {
                return null
            }
        return db.articleContextDao().getNextLargestInUniverse(oldArticle.id!!)
    }
}
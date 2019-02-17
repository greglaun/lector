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

    override fun getArticleContext(context: String): Deferred<ArticleContext?> {
        return GlobalScope.async {
            db.articleContextDao().get(context)
        }
    }

    override fun updatePosition(currentRequestContext: String, position: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().updatePosition(currentRequestContext, position)
        }
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

    override fun getNextArticle(context: String): Deferred<ArticleContext?> {
        return GlobalScope.async {
            val oldArticle = db.articleContextDao().get(context) ?: return@async null
            if (oldArticle.temporary) {
                return@async null
            }
            db.articleContextDao().getNextLargestInUniverse(oldArticle.id!!)
        }
    }
}
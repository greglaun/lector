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

    override fun add(element: String): Deferred<Long> {
        return GlobalScope.async {
            val existingEntry = db.articleContextDao().get(element)
            if (existingEntry == null) {
                db.articleContextDao().insert(RoomArticleContext(null, contextString = element))
            } else {
                existingEntry.id!!
            }
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
            articleContext?.let {
                articleContext.contextString = to
                db.articleContextDao().updateArticleContext(articleContext)
            }
            Unit
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
            db.articleContextDao().getNextLargest(oldArticle.id!!)
        }
    }
}
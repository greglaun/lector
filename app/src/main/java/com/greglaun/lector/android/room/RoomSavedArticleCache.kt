package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.SavedArticleCache
import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.serialize
import com.greglaun.lector.data.cache.toResponse
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.Request
import okhttp3.Response

class RoomSavedArticleCache(var db: ArticleCacheDatabase) :
        SavedArticleCache<Request, Response, String> {
    val idCache = HashMap<String, Long>()

    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
       return GlobalScope.async {
            val cachedResponse = db.cachedResponseDao().get(key.url().toString().md5())
           cachedResponse?.response?.toResponse()
        }
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
            var articleId: Long? = null
            if (idCache.containsKey(keyContext)) {
                articleId = idCache.get(keyContext)
            } else {
                articleId = db.articleContextDao().get(keyContext).id
                idCache.put(keyContext, articleId!!)
            }
            val cachedResponse = CachedResponse(null, key.url().toString().md5(),
                    value.serialize(), articleId!!)
                db.cachedResponseDao().insert(cachedResponse)
            Unit
        }
    }

    override fun garbageCollectContext(keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
            db.cachedResponseDao().deleteWithContext(keyContext)
            db.articleContextDao().delete(keyContext)
            CompletableDeferred(Unit)
            Unit
        }
    }

    override fun addContext(keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
            db.articleContextDao().insert(RoomArticleContext(null, keyContext))
            Unit
        }
    }

    override fun garbageCollectTemporary(classifier: CacheEntryClassifier<String>): Deferred<Unit> {
        return GlobalScope.async {
            // Ignore the passed-in classifier, since we already have access to the db
            db.cachedResponseDao().deleteAllTemporary()
            db.articleContextDao().deleteAllTemporary()
        }
    }
}

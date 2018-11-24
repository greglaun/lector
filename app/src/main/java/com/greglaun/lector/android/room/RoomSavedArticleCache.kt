package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.SavedArticleCache
import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.serialize
import com.greglaun.lector.data.cache.toResponse
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.Request
import okhttp3.Response

class RoomSavedArticleCache(var db: ArticleCacheDatabase) :
        SavedArticleCache<Request, Response, String> {
    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
       return GlobalScope.async {
            val cachedResponse = db.cachedResponseDao().get(key.url().toString().md5())
           cachedResponse?.response?.toResponse()
        }
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
            val cachedResponse = CachedResponse(null, key.url().toString().md5(),
                    value.serialize(), keyContext)
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
            db.articleContextDao().insert(ArticleContext(keyContext))
            Unit
        }
    }
}

package com.greglaun.lector.android.room

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
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

    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
       return GlobalScope.async {
            val cachedResponse = db.cachedResponseDao().get(key.url().toString().md5())
           cachedResponse?.response?.toResponse()
        }
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
            var articleId: Long? = null
            try {
                articleId = db.articleContextDao().get(keyContext)?.id
                if (articleId == null) {
                    articleId = db.articleContextDao().insert(
                            RoomArticleContext(null, keyContext, "", true))
                }
                val cachedResponse = CachedResponse(null, key.url().toString().md5(),
                        value.serialize(), articleId!!)
                db.cachedResponseDao().insert(cachedResponse)
            } catch (e : SQLiteConstraintException) {
                Log.d("RoomSavedArticleCache", keyContext, e)
                throw e
            }
            Unit
        }
    }

    override fun garbageCollectContext(keyContext: String): Deferred<Unit> {
        return GlobalScope.async {
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
            db.articleContextDao().deleteAllTemporary()
        }
    }
}

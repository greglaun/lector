package com.greglaun.lector.android.room

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.greglaun.lector.data.cache.SavedArticleCache
import com.greglaun.lector.data.cache.md5
import com.greglaun.lector.data.cache.serialize
import com.greglaun.lector.data.cache.toResponse
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import okhttp3.Request
import okhttp3.Response

class RoomSavedArticleCache(var db: LectorDatabase) :
        SavedArticleCache<Request, Response, String> {

    override suspend fun getWithContext(key: Request, keyContext: String): Response? {
        val cachedResponse = db.cachedResponseDao().get(key.url().toString().md5())
        return cachedResponse?.response?.toResponse()
    }

    override suspend fun setWithContext(key: Request, value: Response, keyContext: String) {
        var articleId: Long?
        try {
            articleId = db.articleContextDao().get(keyContext)?.id
            if (articleId == null) {
                articleId = db.articleContextDao().insert(
                        RoomArticleContext(null, keyContext, "", true))
            }
            val cachedResponse = CachedResponse(null, key.url().toString().md5(),
                    value.serialize(), articleId!!)
            return db.cachedResponseDao().insert(cachedResponse)
        } catch (e : SQLiteConstraintException) {
            Log.d("RoomSavedArticleCache", keyContext, e)
            throw e
        }
    }

    override suspend fun garbageCollectContext(keyContext: String) {
        db.articleContextDao().delete(keyContext)
    }

    override suspend fun addContext(keyContext: String) {
        db.articleContextDao().insert(RoomArticleContext(null, keyContext))
    }

    override suspend fun garbageCollectTemporary(classifier: CacheEntryClassifier<String>) {
        // Ignore the passed-in classifier, since we already have access to the db
        db.articleContextDao().deleteAllTemporary()
    }
}

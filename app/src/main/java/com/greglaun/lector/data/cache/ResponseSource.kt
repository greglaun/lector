package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response

interface ResponseSource: CacheEntryClassifier<String>, ContextAwareCache<Request, Response, String> {
    fun markTemporary(keyContext: String): Deferred<Unit>
    fun markPermanent(keyContext: String): Deferred<Unit>
    fun garbageCollectTemporary(): Deferred<Unit> {
        return garbageCollectTemporary(this)
    }
}

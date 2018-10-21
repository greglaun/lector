package com.greglaun.lector.cache

import com.greglaun.lector.data.cache.ComposableCache
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Response

class SavedArticleCache(val delegateCache : ReferenceCountingCacheWrapper)  : ComposableCache<String, Response>{

    override fun get(key: String): Deferred<Response?> {
        return delegateCache.wrappedCache.get(key)
    }

    override fun set(key: String, value: Response): Deferred<Unit> {
        // Warning, we are assuming here that everything works okay if we update the cached response
        // for everything that references it. This should be a semi-reasonable assumption for
        // Wikipedia data, since we are dealing mostly with text and images.
        val returnValue =  delegateCache.wrappedCache.set(key, value)
        // Wait until the set succeeds
        delegateCache.setReferenceCount(key, delegateCache.getReferenceCount(key) + 1L)
        return returnValue
    }

    override fun deleteFromTop(key : String) : Deferred<Boolean> {
        val referenceCount = delegateCache.getReferenceCount(key)
        if (referenceCount <= 1L) {
            return delegateCache.wrappedCache.deleteFromTop(key)
        }
        delegateCache.setReferenceCount(key,referenceCount - 1)
        return CompletableDeferred(false)
    }
}
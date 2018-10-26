package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.ProbabilisticSet
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response

class SavedArticleCache(val delegateCache : ReferenceCountingCacheWrapper,
                        val whitelist : ProbabilisticSet<String>)
    : ComposableCache<Request, Response>{

    override fun get(key: Request): Deferred<Response?> {
        return delegateCache.wrappedCache.get(key)
    }

    override fun set(key: Request, value: Response): Deferred<Unit> {
        if (!whitelist.probablyContains(key.url().toString())) {
            // Not whitelisted, do nothing
            return CompletableDeferred(Unit)
        }
        // Warning, we are assuming here that everything works okay if we update the cached response
        // for everything that references it. This should be a semi-reasonable assumption for
        // Wikipedia data, since we are dealing mostly with text and images.
        val returnValue =  delegateCache.wrappedCache.set(key, value)
        // Wait until the set succeeds
        delegateCache.setReferenceCount(key.url().toString(),
                delegateCache.getReferenceCount(key.url().toString()) + 1L)
        return returnValue
    }

    override fun deleteFromTop(key : Request) : Deferred<Boolean> {
        val referenceCount = delegateCache.getReferenceCount(key.url().toString())
        if (referenceCount <= 1L) {
            return delegateCache.wrappedCache.deleteFromTop(key)
        }
        delegateCache.setReferenceCount(key.url().toString(),referenceCount - 1)
        return CompletableDeferred(false)
    }
}
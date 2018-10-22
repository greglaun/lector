package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response

// An in-memory cache to be used for testing
class HashMapResponseCache : ComposableCache<Request, Response> {
    val hashCache : HashMap<Request, Response> = HashMap()

    override fun get(key: Request): Deferred<Response?> {
        return CompletableDeferred(hashCache.get(key))
    }

    override fun set(key: Request, value: Response): Deferred<Unit> {
        hashCache.put(key, value)
        return CompletableDeferred(Unit)
    }

}


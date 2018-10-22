package com.greglaun.lector.data.cache

import okhttp3.Request
import okhttp3.Response

// An in-memory ReferenceCountingWrapper to use for testing
class HashMapReferenceCountingWrapper(override val wrappedCache: ComposableCache<Request, Response>) : ReferenceCountingCacheWrapper {
    val referenceMap : HashMap<String, Long> = HashMap()

    override fun getReferenceCount(key: String): Long {
        return referenceMap.getOrDefault(key, 0L)
    }

    override fun setReferenceCount(key: String, newCount: Long) {
        referenceMap.set(key, newCount)
    }
}
package com.greglaun.lector.cache

import com.greglaun.lector.data.cache.ComposableCache
import okhttp3.Response

class HashMapReferenceCountingWrapper(override val wrappedCache: ComposableCache<String, Response>) : ReferenceCountingCacheWrapper {
    val referenceMap : HashMap<String, Long> = HashMap()

    override fun getReferenceCount(key: String): Long {
        return referenceMap.getOrDefault(key, 0L)
    }

    override fun setReferenceCount(key: String, newCount: Long) {
        referenceMap.set(key, newCount)
    }
}
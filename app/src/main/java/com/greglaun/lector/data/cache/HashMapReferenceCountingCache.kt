package com.greglaun.lector.data.cache

import com.greglaun.lector.data.container.ProbabilisticSet
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

    override fun deleteNotWhitelisted(whitelist: ProbabilisticSet<String>) : Int {
        var counter = 0
        referenceMap.forEach {
            (key, value) ->
            if (!whitelist.probablyContains(key)) {
                referenceMap.remove(key)
                counter += 1
            }
        }
        return counter
    }
}
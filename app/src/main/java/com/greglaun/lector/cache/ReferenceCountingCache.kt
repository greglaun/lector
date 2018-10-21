package com.greglaun.lector.cache

import com.greglaun.lector.data.cache.ComposableCache
import okhttp3.Response

interface ReferenceCountingCacheWrapper {
    val wrappedCache : ComposableCache<String, Response>
    fun getReferenceCount(key : String) : Long
    fun setReferenceCount(key : String, newCount : Long)
}

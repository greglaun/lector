package com.greglaun.lector.data.cache

import okhttp3.Request
import okhttp3.Response

interface ReferenceCountingCacheWrapper {
    val wrappedCache : ComposableCache<Request, Response>
    fun getReferenceCount(urlString : String) : Long
    fun setReferenceCount(urlString : String, newCount : Long)
}



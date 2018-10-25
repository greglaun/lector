package com.greglaun.lector.data.cache

import com.greglaun.lector.data.container.ProbabilisticSet
import okhttp3.Request
import okhttp3.Response

interface ReferenceCountingCacheWrapper {
    val wrappedCache : ComposableCache<Request, Response>
    fun getReferenceCount(urlString : String) : Long
    fun setReferenceCount(urlString : String, newCount : Long)

    // Iterate over all items in the cache and delete if the url string does is not on the whitelist
    // return  the number of items deleted
    fun deleteNotWhitelisted(whitelist : ProbabilisticSet<String>) : Int
}



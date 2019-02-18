package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import okhttp3.Request
import okhttp3.Response

interface ResponseSource: CacheEntryClassifier<String>, ContextAwareCache<Request, Response, String> {
    suspend fun garbageCollectTemporary() {
        return garbageCollectTemporary(this)
    }
}

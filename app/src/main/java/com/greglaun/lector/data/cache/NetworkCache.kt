package com.greglaun.lector.data.cache

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

// A "cache" that calls out to the network. This inverts the traditional approach of calling out
// to the network and possibly (opaquely to the caller) returning a cached responses. There may
// be no good reason to do this, so this may change in the future.
open class NetworkCache(val httpClient : OkHttpClient)
    : ComposableCache<Request, Response> {

    override suspend fun get(key: Request): Response? {
        return try {
            httpClient.newCall(key).execute()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun set(key: Request, value: Response) {
        // Do nothing
    }
}

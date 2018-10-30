package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

// A "cache" that calls out to the network. This inverts the traditional approach of calling out
// to the network and possibly (opaquely to the caller) returning a cached responses. There may
// be no good reason to do this, so this may change in the future.
class NetworkCache(val httpClient : OkHttpClient)
    : ComposableCache<Request, Response> {

    override fun get(key: Request): Deferred<Response> {
        return GlobalScope.async {
            httpClient.newCall(key).execute()
        }
    }
    override fun set(key: Request, value: Response): Deferred<Unit> {
        // Do nothing
        return CompletableDeferred(Unit)
    }
}

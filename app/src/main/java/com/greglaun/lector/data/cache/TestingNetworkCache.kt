package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class TestingNetworkCache(httpClient: OkHttpClient) : NetworkCache(httpClient) {
    var disableNetwork = false

    override fun get(key: Request): Deferred<Response?> {
        if (disableNetwork) {
            val response: Response? = null
            return CompletableDeferred(response)
        }
        return GlobalScope.async {
            try {
                httpClient.newCall(key).execute()
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun set(key: Request, value: Response): Deferred<Unit> {
        // Do nothing
        return CompletableDeferred(Unit)
    }

    fun disableNetwork(shouldDisable: Boolean) {
        disableNetwork = shouldDisable
    }
}
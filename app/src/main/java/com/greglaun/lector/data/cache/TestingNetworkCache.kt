package com.greglaun.lector.data.cache

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class TestingNetworkCache(httpClient: OkHttpClient) : NetworkCache(httpClient) {
    var disableNetwork = false

    override suspend fun get(key: Request): Response? {
        if (disableNetwork) {
            return null
        }
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
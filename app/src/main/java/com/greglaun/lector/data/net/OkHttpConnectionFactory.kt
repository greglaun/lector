package com.greglaun.lector.data.net

import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

object OkHttpConnectionFactory {
    private val CACHE_SIZE = (64 * 1024 * 1024).toLong()

    fun createClient(cacheDirectory: File,
                     cacheSize : Long = CACHE_SIZE): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(Cache(cacheDirectory, cacheSize))
                .build()
    }
}

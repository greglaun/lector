package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import okhttp3.Request
import okhttp3.Response
import java.io.File

// A composition of caches that first looks for saved articles, and if that fails, calls out to the
// network. Also uses the DiskLruCache from OkHttp, although that is not (currently?) implemented
// as a composable cache.
object ResponseSourceFactory {

    fun createResponseSource(savedArticleCache: SavedArticleCache<Request, Response, String>,
                             lruCacheDir: File): ContextAwareCache<Request, Response, String> {
        return savedArticleCache.compose(
                NetworkCache(
                        OkHttpConnectionFactory.createClient(lruCacheDir)))
    }
}
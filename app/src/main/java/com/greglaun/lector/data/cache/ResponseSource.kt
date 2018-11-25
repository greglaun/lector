package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.whitelist.Whitelist
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response
import java.io.File

// A composition of caches that first looks for saved articles, and if that fails, calls out to the
// network. Also uses the DiskLruCache from OkHttp, although that is not (currently?) implemented
// as a composable cache.
class ResponseSource(val articleCache: ContextAwareCache<Request, Response, String>,
                     val whitelist: Whitelist<String>):
        Whitelist<String>, ContextAwareCache<Request, Response, String> {
    companion object {
        fun createResponseSource(savedArticleCache : SavedArticleCache<Request, Response, String>,
                                 whitelist: Whitelist<String>,
                                 lruCacheDir : File)
                : ResponseSource {
            return ResponseSource(WhitelistSavedArticleCache(savedArticleCache, whitelist).compose(
                    NetworkCache(
                            OkHttpConnectionFactory.createClient(lruCacheDir))), whitelist)
        }

    }
    override fun contains(element: String): Deferred<Boolean> {
        return whitelist.contains(element)
    }

    override fun add(element: String): Deferred<Unit> {
        return whitelist.add(element)
    }

    override fun delete(element: String): Deferred<Unit> {
        return whitelist.delete(element)
    }

    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
        return articleCache.getWithContext(key, keyContext)
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return articleCache.setWithContext(key, value, keyContext)
    }

    override fun iterator(): Iterator<String> {
        return whitelist.iterator()
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        return whitelist.update(from, to)
    }
}
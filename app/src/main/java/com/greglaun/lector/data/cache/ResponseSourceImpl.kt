package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response
import java.io.File

// A composition of caches that first looks for saved articles, and if that fails, calls out to the
// network. Also uses the DiskLruCache from OkHttp
class ResponseSourceImpl(val articleCache: ContextAwareCache<Request, Response, String>,
                         val cacheEntryClassifier: CacheEntryClassifier<String>):
        ResponseSource {
    companion object {
        fun createResponseSource(savedArticleCache : SavedArticleCache<Request, Response, String>,
                                 cacheEntryClassifier: CacheEntryClassifier<String>,
                                 lruCacheDir : File)
                : ResponseSourceImpl {
            return ResponseSourceImpl(savedArticleCache.compose(
                    NetworkCache(
                            OkHttpConnectionFactory.createClient(lruCacheDir))), cacheEntryClassifier)
        }

    }
    override fun contains(element: String): Deferred<Boolean> {
        return cacheEntryClassifier.contains(element)
    }

    override fun add(element: String): Deferred<Unit> {
        return cacheEntryClassifier.add(element)
    }

    override fun delete(element: String): Deferred<Unit> {
        return cacheEntryClassifier.delete(element)
    }

    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
        return articleCache.getWithContext(key, keyContext)
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return articleCache.setWithContext(key, value, keyContext)
    }

    override fun iterator(): Iterator<String> {
        return cacheEntryClassifier.iterator()
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        return cacheEntryClassifier.update(from, to)
    }

    override fun markTemporary(keyContext: String): Deferred<Unit> {
        return cacheEntryClassifier.markTemporary(keyContext)
    }

    override fun markPermanent(keyContext: String): Deferred<Unit> {
        return cacheEntryClassifier.markPermanent(keyContext)
    }

    override fun garbageCollectTemporary(): Deferred<Unit> {
        return articleCache.garbageCollectTemporary(cacheEntryClassifier)
    }

    override fun garbageCollectContext(keyContext: String): Deferred<Unit> {
        return articleCache.garbageCollectContext(keyContext)
    }

    override fun garbageCollectTemporary(classifier: CacheEntryClassifier<String>): Deferred<Unit> {
        return articleCache.garbageCollectTemporary(classifier)
    }

    override fun getAllTemporary(): Deferred<ListIterator<String>> {
        return cacheEntryClassifier.getAllTemporary()
    }
}
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
                                 networkCache: NetworkCache)
                : ResponseSourceImpl {
            return ResponseSourceImpl(savedArticleCache.compose(networkCache), cacheEntryClassifier)

        }

        fun createResponseSource(savedArticleCache : SavedArticleCache<Request, Response, String>,
                                 cacheEntryClassifier: CacheEntryClassifier<String>,
                                 lruCacheDir : File): ResponseSourceImpl {
            return createResponseSource(savedArticleCache, cacheEntryClassifier,
                    NetworkCache(OkHttpConnectionFactory.createClient(lruCacheDir)))
        }
    }

    override suspend fun contains(element: String): Boolean {
        return cacheEntryClassifier.contains(element)
    }

    override suspend fun add(element: String): Long {
        return cacheEntryClassifier.add(element)
    }

    override suspend fun delete(element: String) {
        return cacheEntryClassifier.delete(element)
    }

    override fun getWithContext(key: Request, keyContext: String): Deferred<Response?> {
        return articleCache.getWithContext(key, keyContext)
    }

    override fun setWithContext(key: Request, value: Response, keyContext: String): Deferred<Unit> {
        return articleCache.setWithContext(key, value, keyContext)
    }

    override suspend fun update(from: String, to: String) {
        cacheEntryClassifier.update(from, to)
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

    override fun getAllTemporary(): Deferred<List<ArticleContext>> {
        return cacheEntryClassifier.getAllTemporary()
    }

    override fun isTemporary(element: String): Deferred<Boolean> {
        return cacheEntryClassifier.isTemporary(element)
    }

    override fun getAllPermanent(): Deferred<List<ArticleContext>> {
        return cacheEntryClassifier.getAllPermanent()
    }

    override fun getArticleContext(context: String): Deferred<ArticleContext?> {
        return cacheEntryClassifier.getArticleContext(context)
    }

    override fun updatePosition(context: String, position: String): Deferred<Unit> {
        return cacheEntryClassifier.updatePosition(context, position)
    }

    override fun getUnfinished(): Deferred<List<String>> {
        return cacheEntryClassifier.getUnfinished()
    }

    override fun markFinished(element: String): Deferred<Unit> {
        return cacheEntryClassifier.markFinished(element)
    }

    override fun getNextArticle(context: String): Deferred<ArticleContext?> {
        return cacheEntryClassifier.getNextArticle(context)
    }
}
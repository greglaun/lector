package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
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

    override suspend fun getWithContext(key: Request, keyContext: String): Response? {
        return articleCache.getWithContext(key, keyContext)
    }

    override suspend fun setWithContext(key: Request, value: Response, keyContext: String) {
        return articleCache.setWithContext(key, value, keyContext)
    }

    override suspend fun update(from: String, to: String) {
        cacheEntryClassifier.update(from, to)
    }

    override suspend fun markTemporary(keyContext: String) {
        return cacheEntryClassifier.markTemporary(keyContext)
    }

    override suspend fun markPermanent(keyContext: String) {
        return cacheEntryClassifier.markPermanent(keyContext)
    }

    override suspend fun garbageCollectTemporary() {
        return articleCache.garbageCollectTemporary(cacheEntryClassifier)
    }

    override suspend fun garbageCollectContext(keyContext: String) {
        return articleCache.garbageCollectContext(keyContext)
    }

    override suspend fun garbageCollectTemporary(classifier: CacheEntryClassifier<String>) {
        return articleCache.garbageCollectTemporary(classifier)
    }

    override suspend fun getAllTemporary(): List<ArticleContext> {
        return cacheEntryClassifier.getAllTemporary()
    }

    override suspend fun isTemporary(element: String): Boolean {
        return cacheEntryClassifier.isTemporary(element)
    }

    override suspend fun getAllPermanent(): List<ArticleContext>? {
        return cacheEntryClassifier.getAllPermanent()
    }

    override suspend fun getArticleContext(context: String): ArticleContext? {
        return cacheEntryClassifier.getArticleContext(context)
    }

    override suspend fun updatePosition(context: String, position: String) {
        return cacheEntryClassifier.updatePosition(context, position)
    }

    override suspend fun getUnfinished(): List<String> {
        return cacheEntryClassifier.getUnfinished()
    }

    override suspend fun markFinished(element: String) {
        return cacheEntryClassifier.markFinished(element)
    }

    override suspend fun getNextArticle(context: String): ArticleContext? {
        return cacheEntryClassifier.getNextArticle(context)
    }
}
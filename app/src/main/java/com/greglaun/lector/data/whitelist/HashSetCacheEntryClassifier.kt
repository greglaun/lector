package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.*
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

// A deterministic probabilistic set for testing
class HashSetCacheEntryClassifier: CacheEntryClassifier<String> {
    val hashMap = HashMap<String, BasicArticleContext>()

    override suspend fun contains(element: String): Boolean {
        return hashMap.contains(element)
    }

    override fun add(element : String): Deferred<Long> {
        hashMap.put(element, BasicArticleContext.fromString(element))
        return CompletableDeferred(1L)
    }

    override fun delete(element: String): Deferred<Unit> {
        hashMap.remove(element)
        return CompletableDeferred(Unit)
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        if (hashMap.contains(from)) {
            hashMap.remove(from)
            hashMap.put(to, BasicArticleContext.fromString(to))
        }
        return CompletableDeferred(Unit)
    }

    override fun getAllTemporary(): Deferred<List<ArticleContext>> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (it.value.temporary) {
                result.add(it.value)
            }
        }
        return CompletableDeferred(result.toList())
    }

    override fun markTemporary(element: String): Deferred<Unit> {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.makeTemporary())
        }
        return CompletableDeferred(Unit)
    }

    override fun markPermanent(element: String): Deferred<Unit> {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.makePermanent())
        }
        return CompletableDeferred(Unit)
    }

    override fun isTemporary(element: String): Deferred<Boolean> {
        if (hashMap.containsKey(element)) {
            return CompletableDeferred(hashMap.get(element)!!.temporary)
        }
        return CompletableDeferred(true)
    }

    override fun getAllPermanent(): Deferred<List<ArticleContext>> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (!it.value.temporary) {
                result.add(it.value)
            }
        }
        return CompletableDeferred(result.toList())
    }

    override fun getArticleContext(context: String): Deferred<ArticleContext?> {
        return CompletableDeferred(hashMap.get(context))
    }

    override fun updatePosition(currentRequestContext: String, position: String): Deferred<Unit> {
        val originalEntry = hashMap.get(currentRequestContext)
        originalEntry?.let {
            hashMap.put(currentRequestContext, originalEntry.updatePosition(position))
        }
        return CompletableDeferred(Unit)
    }

    override fun getUnfinished(): Deferred<List<String>> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (!it.value.downloadComplete) {
                result.add(it.value)
            }
        }
        return CompletableDeferred(result.toList().map { it.contextString })
    }

    override fun markFinished(element: String): Deferred<Unit> {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.markDownloadComplete())
        }
        return CompletableDeferred(Unit)
    }

    override fun getNextArticle(context: String): Deferred<ArticleContext?> {
        return CompletableDeferred(null)
    }
}
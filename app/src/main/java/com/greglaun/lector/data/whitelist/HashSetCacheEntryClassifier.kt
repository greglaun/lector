package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.BasicArticleContext
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

// A deterministic probabilistic set for testing
class HashSetCacheEntryClassifier: CacheEntryClassifier<String> {
    val hashMap = HashMap<String, Boolean>()

    override fun contains(element: String): Deferred<Boolean> {
        return CompletableDeferred(hashMap.contains(element))
    }

    override fun add(element : String): Deferred<Unit> {
        hashMap.put(element, true)
        return CompletableDeferred(Unit)
    }

    override fun delete(element: String): Deferred<Unit> {
        hashMap.remove(element)
        return CompletableDeferred(Unit)
    }

    override fun update(from: String, to: String): Deferred<Unit> {
        if (hashMap.contains(from)) {
            hashMap.remove(from)
        }
        return CompletableDeferred(Unit)
    }

    override fun getAllTemporary(): Deferred<List<ArticleContext>> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (it.value) {
                result.add(BasicArticleContext.fromString(it.key))
            }
        }
        return CompletableDeferred(result.toList())
    }

    override fun markTemporary(element: String): Deferred<Unit> {
        hashMap.put(element, true)
        return CompletableDeferred(Unit)
    }

    override fun markPermanent(element: String): Deferred<Unit> {
        hashMap.put(element, false)
        return CompletableDeferred(Unit)
    }

    override fun isTemporary(element: String): Deferred<Boolean> {
        if (hashMap.containsKey(element)) {
            return CompletableDeferred(hashMap.get(element)!!)
        }
        return CompletableDeferred(true)
    }

    override fun getAllPermanent(): Deferred<List<ArticleContext>> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (!it.value) {
                result.add(BasicArticleContext(1L, it.key, "", false))
            }
        }
        return CompletableDeferred(result.toList())
    }
}
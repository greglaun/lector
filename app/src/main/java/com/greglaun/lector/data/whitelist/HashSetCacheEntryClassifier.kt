package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.*

// A deterministic probabilistic set for testing
class HashSetCacheEntryClassifier: CacheEntryClassifier<String> {
    val hashMap = HashMap<String, BasicArticleContext>()

    override suspend fun contains(element: String): Boolean {
        return hashMap.contains(element)
    }

    override suspend fun add(element : String): Long {
        hashMap.put(element, BasicArticleContext.fromString(element))
        return 1L
    }

    override suspend fun delete(element: String) {
        hashMap.remove(element)
    }

    override suspend fun update(from: String, to: String) {
        if (hashMap.contains(from)) {
            hashMap.remove(from)
            hashMap.put(to, BasicArticleContext.fromString(to))
        }
    }

    override suspend fun getAllTemporary(): List<ArticleContext> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (it.value.temporary) {
                result.add(it.value)
            }
        }
        return result.toList()
    }

    override suspend fun markTemporary(element: String) {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.makeTemporary())
        }
    }

    override suspend fun markPermanent(element: String) {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.makePermanent())
        }
    }

    override suspend fun isTemporary(element: String): Boolean {
        if (hashMap.containsKey(element)) {
            return hashMap.get(element)!!.temporary
        }
        return true
    }

    override suspend fun getAllPermanent(): List<ArticleContext> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (!it.value.temporary) {
                result.add(it.value)
            }
        }
        return result.toList()
    }

    override suspend fun getArticleContext(context: String): ArticleContext? {
        return hashMap.get(context)
    }

    override suspend fun updatePosition(currentRequestContext: String, position: String) {
        val originalEntry = hashMap.get(currentRequestContext)
        originalEntry?.let {
            hashMap.put(currentRequestContext, originalEntry.updatePosition(position))
        }
    }

    override suspend fun getUnfinished(): List<String> {
        val result = ArrayList<ArticleContext>()
        hashMap.forEach{
            if (!it.value.downloadComplete) {
                result.add(it.value)
            }
        }
        return result.toList().map { it.contextString }
    }

    override suspend fun markFinished(element: String) {
        val originalEntry = hashMap.get(element)
        originalEntry?.let {
            hashMap.put(element, originalEntry.markDownloadComplete())
        }
    }

    override suspend fun getNextArticle(context: String): ArticleContext? {
        return null
    }
}
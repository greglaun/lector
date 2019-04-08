package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import okhttp3.Request
import okhttp3.Response
import java.util.*

// An in-memory cache to be used for testing
class HashMapSavedArticleCache : SavedArticleCache<Request, Response, String> {
    private val hashCache : HashMap<Request, Pair<String, HashSet<String>>> = HashMap()

    override suspend fun getWithContext(key: Request, keyContext : String): Response? {
        if (!hashCache.containsKey(key)) {
            return null
        }
        if (!hashCache[key]!!.second.contains(keyContext)) {
            return null
        }
        return hashCache[key]!!.first.toResponse()

    }

    override suspend fun setWithContext(key: Request, value: Response, keyContext : String) {
        if (hashCache.containsKey(key)) {
            val cacheEntry = hashCache[key]!!
            cacheEntry.second.add(keyContext)
        } else {
            val set = HashSet<String>()
            set.add(keyContext)
            hashCache[key] = Pair(value.serialize(), set)
        }
    }

    override suspend fun garbageCollectContext(keyContext : String) {
        val iterator = hashCache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.second.remove(keyContext)
            if (entry.value.second.isEmpty()) {
                    iterator.remove()
                }
        }
    }

    override suspend fun addContext(keyContext: String) {
        // Do nothing
    }

    override suspend fun garbageCollectTemporary(classifier: CacheEntryClassifier<String>) {
        return hashCache.forEach {
            val iterator = it.value.second.iterator()
            while (iterator.hasNext()) {
                val iter = iterator.next()
                if (classifier.isTemporary(iter)
                ) {
                    iterator.remove()
                }
            }
        }
    }
}


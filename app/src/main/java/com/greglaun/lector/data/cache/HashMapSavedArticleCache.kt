package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import okhttp3.Request
import okhttp3.Response
import java.util.*

// An in-memory cache to be used for testing
class HashMapSavedArticleCache : SavedArticleCache<Request, Response, String> {
    val hashCache : HashMap<Request, Pair<Response, HashSet<String>>> = HashMap()

    override fun getWithContext(key: Request, keyContext : String)
            : Deferred<Response?> {
        if (!hashCache.containsKey(key)) {
            return CompletableDeferred(value = null)
        }
        return CompletableDeferred(hashCache.get(key)!!.first)

    }

    override fun setWithContext(key: Request, value: Response, keyContext : String): Deferred<Unit> {
        if (hashCache.containsKey(key)) {
            val cacheEntry = hashCache.get(key)!!
            cacheEntry.second.add(keyContext)
        } else {
            val set = HashSet<String>()
            set.add(keyContext)
            hashCache.put(key, Pair(value, set))
        }
        return CompletableDeferred(Unit)
    }

    override fun garbageCollectContext(keyContext : String) {
        val iterator = hashCache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.second.remove(keyContext)
            if (entry.value.second.isEmpty()) {
                    iterator.remove()
                }
        }
    }
}


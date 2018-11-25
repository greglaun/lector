package com.greglaun.lector.data.whitelist

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

// A deterministic probabilistic set for testing
class HashSetCacheEntryClassifier<T>: CacheEntryClassifier<T> {
    val hashMap = HashMap<T, Boolean>()

    override fun contains(element: T): Deferred<Boolean> {
        return CompletableDeferred(hashMap.contains(element))
    }

    override fun add(element : T): Deferred<Unit> {
        hashMap.put(element, true)
        return CompletableDeferred(Unit)
    }

    override fun delete(element: T): Deferred<Unit> {
        hashMap.remove(element)
        return CompletableDeferred(Unit)
    }

    override fun update(from: T, to: T): Deferred<Unit> {
        if (hashMap.contains(from)) {
            hashMap.remove(from)
        }
        return CompletableDeferred(Unit)
    }

    override fun getAllTemporary(): Deferred<List<T>> {
        val result = ArrayList<T>()
        hashMap.forEach{
            if (it.value) {
                result.add(it.key)
            }
        }
        return CompletableDeferred(result.toList())
    }

    override fun markTemporary(element: T): Deferred<Unit> {
        hashMap.put(element, true)
        return CompletableDeferred(Unit)
    }

    override fun markPermanent(element: T): Deferred<Unit> {
        hashMap.put(element, false)
        return CompletableDeferred(Unit)
    }

    override fun isTemporary(element: T): Deferred<Boolean> {
        if (hashMap.containsKey(element)) {
            return CompletableDeferred(hashMap.get(element)!!)
        }
        return CompletableDeferred(true)
    }
}
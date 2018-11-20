package com.greglaun.lector.data.whitelist

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

// A deterministic probabilistic set for testing
class HashSetWhitelist<T>: Whitelist<T> {
    val hashSet = HashSet<T>()
    override fun contains(element: T): Deferred<Boolean> {
        return CompletableDeferred(hashSet.contains(element))
    }

    override fun add(element : T): Deferred<Unit> {
        hashSet.add(element)
        return CompletableDeferred(Unit)
    }

    override fun delete(element: T): Deferred<Unit> {
        hashSet.remove(element)
        return CompletableDeferred(Unit)
    }

    override fun iterator(): MutableIterator<T> {
        return hashSet.iterator()
    }
}
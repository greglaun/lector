package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

interface ComposableCache<Key : Any, Value : Any> {
    fun get(key: Key): Deferred<Value?>
    fun set(key: Key, value: Value): Deferred<Unit>

    // Delete only from the top wrappedCache, do not propagate deletes to other caches in the chain.
    // Returns true if the delete successfully removed an item and false otherwise
    fun deleteFromTop(key: Key) : Deferred<Boolean> {
        // Assume a trivial implementation of deleteContextFromTop
        return CompletableDeferred(false)
    }

    fun compose(b: ComposableCache<Key, Value>):
            ComposableCache<Key, Value> {
        return object : ComposableCache<Key, Value> {
            override fun get(key: Key): Deferred<Value?> {
                return GlobalScope.async {
                    this@ComposableCache.get(key).await() ?: let {
                        b.get(key).await()?.apply {
                            this@ComposableCache.set(key, this).await()
                        }
                    }
                }
            }

            override fun set(key: Key, value: Value): Deferred<Unit> {
                return GlobalScope.async {
                    listOf(this@ComposableCache.set(key, value),
                            b.set(key, value)).forEach { it.await() }
                }
            }
        }
    }
}
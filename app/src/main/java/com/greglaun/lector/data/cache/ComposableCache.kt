package com.greglaun.lector.data.cache

interface ComposableCache<Key : Any, Value : Any> {
    suspend fun get(key: Key): Value?
    suspend fun set(key: Key, value: Value)

    // Delete only from the top wrappedCache, do not propagate deletes to other caches in the chain.
    // Returns true if the delete successfully removed an item and false otherwise
    suspend fun deleteFromTop(key: Key): Boolean {
        // Assume a trivial implementation of deleteContextFromTop
        return false
    }

    fun compose(b: ComposableCache<Key, Value>):
            ComposableCache<Key, Value> {
        return object : ComposableCache<Key, Value> {
            override suspend fun get(key: Key): Value? {
                return this@ComposableCache.get(key) ?: let {
                    b.get(key)?.apply {
                        this@ComposableCache.set(key, this)
                    }
                }
            }

            override suspend fun set(key: Key, value: Value) {
                    listOf(this@ComposableCache.set(key, value),
                            b.set(key, value)).forEach { it }
            }
        }
    }
}
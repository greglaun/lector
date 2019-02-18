package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.CacheEntryClassifier

interface ContextAwareCache<Key : Any, Value : Any, KeyContext : Any> {
    suspend fun getWithContext(key : Key, keyContext : KeyContext) : Value?
    suspend fun setWithContext(key : Key, value : Value, keyContext : KeyContext)
    suspend fun garbageCollectContext(keyContext: String)
    suspend fun garbageCollectTemporary(classifier: CacheEntryClassifier<KeyContext>)

    fun compose(b: ContextAwareCache<Key, Value, KeyContext>)
            : ContextAwareCache<Key, Value, KeyContext> {
        return object : ContextAwareCache<Key, Value, KeyContext> {
            override suspend fun garbageCollectContext(keyContext: String) {
                return this@ContextAwareCache.garbageCollectContext(keyContext)
            }

            override suspend fun garbageCollectTemporary(
                    classifier: CacheEntryClassifier<KeyContext>) {
                return this@ContextAwareCache.garbageCollectTemporary(classifier)
            }

            override suspend fun getWithContext(key: Key, keyContext: KeyContext): Value? {
                return this@ContextAwareCache.getWithContext(key, keyContext) ?: let {
                        b.getWithContext(key, keyContext)?.apply {
                            this@ContextAwareCache.setWithContext(key, this, keyContext)
                        }
                }
            }

            override suspend fun setWithContext(key: Key, value: Value, keyContext: KeyContext) {
                return listOf(this@ContextAwareCache.setWithContext(key, value, keyContext),
                            b.setWithContext(key, value, keyContext)).forEach {it}
            }
        }
    }

    // Throw away contextString when composing with a ComposableCache
    fun compose(b: ComposableCache<Key, Value>):
            ContextAwareCache<Key, Value, KeyContext> {
        return object : ContextAwareCache<Key, Value, KeyContext> {
            override suspend fun garbageCollectContext(keyContext: String) {
                return this@ContextAwareCache.garbageCollectContext(keyContext)
            }

            override suspend fun garbageCollectTemporary(
                    classifier: CacheEntryClassifier<KeyContext>) {
                return this@ContextAwareCache.garbageCollectTemporary(classifier)
            }

            override suspend fun getWithContext(key: Key, keyContext: KeyContext): Value? {
                return this@ContextAwareCache.getWithContext(key, keyContext) ?: let {
                    b.get(key)?.apply {
                        this@ContextAwareCache.setWithContext(key, this, keyContext)
                    }
                }
            }

            override suspend fun setWithContext(key: Key, value: Value, keyContext : KeyContext) {
                return listOf(this@ContextAwareCache.setWithContext(key, value, keyContext),
                        b.set(key, value)).forEach { it }
            }
        }
    }
}

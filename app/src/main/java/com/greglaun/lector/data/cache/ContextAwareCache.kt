package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

interface ContextAwareCache<Key : Any, Value : Any, KeyContext : Any> {
    fun getWithContext(key : Key, keyContext : KeyContext) : Deferred<Value?>
    fun setWithContext(key : Key, value : Value, keyContext : KeyContext) : Deferred<Unit>


    fun compose(b: ContextAwareCache<Key, Value, KeyContext>)
            : ContextAwareCache<Key, Value, KeyContext> {
        return object : ContextAwareCache<Key, Value, KeyContext> {
            override fun getWithContext(key: Key, keyContext: KeyContext): Deferred<Value?> {
                return GlobalScope.async {
                    this@ContextAwareCache.getWithContext(key, keyContext).await() ?: let {
                        b.getWithContext(key, keyContext).await()?.apply {
                            this@ContextAwareCache.setWithContext(key, this, keyContext).await()
                        }
                    }
                }
            }

            override fun setWithContext(key: Key, value: Value, keyContext: KeyContext): Deferred<Unit> {
                return GlobalScope.async {
                    listOf(this@ContextAwareCache.setWithContext(key, value, keyContext),
                            b.setWithContext(key, value, keyContext)).forEach {it.await()}
                }
            }
        }
    }

    // Throw away context when componing with a ComposableCache
    fun compose(b: ComposableCache<Key, Value>):
            ContextAwareCache<Key, Value, KeyContext> {
        return object : ContextAwareCache<Key, Value, KeyContext> {
            override fun getWithContext(key: Key, keyContext : KeyContext): Deferred<Value?> {
                return GlobalScope.async {
                    this@ContextAwareCache.getWithContext(key, keyContext).await() ?: let {
                        b.get(key).await()?.apply {
                            this@ContextAwareCache.setWithContext(key, this, keyContext).await()
                        }
                    }
                }
            }

            override fun setWithContext(key: Key, value: Value, keyContext : KeyContext)
                    : Deferred<Unit> {
                return GlobalScope.async {
                    listOf(this@ContextAwareCache.setWithContext(key, value, keyContext),
                            b.set(key, value)).forEach { it.await() }
                }
            }
        }
    }

}
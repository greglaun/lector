package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.Whitelist
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

// todo(simplicity): Clean up this cache hierarchy
class WhitelistSavedArticleCache<Key : Any, Value : Any, KeyContext : Any>
(val delegateCache : SavedArticleCache<Key, Value, KeyContext>,
 val whitelist : Whitelist<KeyContext>)
    : SavedArticleCache<Key, Value, KeyContext> {
    override fun garbageCollectContext(keyContext: KeyContext): Deferred<Unit> {
        delegateCache.garbageCollectContext(keyContext)
        return CompletableDeferred(Unit)
    }

    override fun getWithContext(key: Key, keyContext: KeyContext): Deferred<Value?> {
       return delegateCache.getWithContext(key, keyContext)
    }

    override fun setWithContext(key: Key, value: Value, keyContext: KeyContext): Deferred<Unit> {
        return delegateCache.setWithContext(key, value, keyContext)
    }

    override fun addContext(keyContext: KeyContext): Deferred<Unit> {
        whitelist.add(keyContext)
        delegateCache.addContext(keyContext)
        return CompletableDeferred(Unit)
    }
}
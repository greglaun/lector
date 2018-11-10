package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.ProbabilisticSet
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred

class WhitelistSavedArticleCache<Key : Any, Value : Any, KeyContext : Any>
(val delegateCache : SavedArticleCache<Key, Value, KeyContext>,
 val whitelist : ProbabilisticSet<KeyContext>)

    : SavedArticleCache<Key, Value, KeyContext> {
    override fun garbageCollectContext(keyContext: KeyContext) {
        delegateCache.garbageCollectContext(keyContext)
    }

    override fun getWithContext(key: Key, keyContext: KeyContext): Deferred<Value?> {
       return delegateCache.getWithContext(key, keyContext)
    }

    override fun setWithContext(key: Key, value: Value, keyContext: KeyContext): Deferred<Unit> {
        if (whitelist.probablyContains(keyContext)) {
            return delegateCache.setWithContext(key, value, keyContext)
        }
        return CompletableDeferred(Unit)
    }
}
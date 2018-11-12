package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.Deferred

interface SavedArticleCache<Key : Any, Value : Any, KeyContext : Any>
    : ContextAwareCache<Key, Value, KeyContext> {

    // Add the key articleContext to the cache, so that it is available to entries that need it.
    // This is for caches that are normalized.
    fun addContext(keyContext: KeyContext): Deferred<Unit>

    // Garbage collect the items from the given articleContext provided they are not referred to by another
    // articleContext. This prevents us from deleting items that are still needed in other contexts.
    //
    // In this sense, a articleContext can be considered like a reference, and we are deleting objects
    // only if their reference count is 1 or 0. Otherwise we "decrement the count" by simply
    // removing that articleContext.
    //
    // Basic algorithm: iterate through the list. If the given articleContext is the only articleContext
    // associated with an item, delete that item. Otherwise remove the articleContext from that item.
    fun garbageCollectContext(keyContext : KeyContext): Deferred<Unit>
}
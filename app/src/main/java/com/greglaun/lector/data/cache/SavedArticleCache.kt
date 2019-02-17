package com.greglaun.lector.data.cache

interface SavedArticleCache<Key : Any, Value : Any, KeyContext : Any>
    : ContextAwareCache<Key, Value, KeyContext> {

    // Add the key contextString to the cache, so that it is available to entries that need it.
    // This is for caches that are normalized.
    suspend fun addContext(keyContext: KeyContext)

    // Garbage collect the items from the given contextString provided they are not referred to by another
    // contextString. This prevents us from deleting items that are still needed in other contexts.
    //
    // In this sense, a contextString can be considered like a reference, and we are deleting objects
    // only if their reference count is 1 or 0. Otherwise we "decrement the count" by simply
    // removing that contextString.
    //
    // Basic algorithm: iterate through the list. If the given contextString is the only contextString
    // associated with an item, delete that item. Otherwise remove the contextString from that item.
    suspend fun garbageCollectContext(keyContext : KeyContext)
}
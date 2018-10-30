package com.greglaun.lector.data.cache

interface SavedArticleCache<Key : Any, Value : Any, KeyContext : Any>
    : ContextAwareCache<Key, Value, KeyContext> {
    

    // Garbage collect the items from the given context provided they are not referred to by another
    // context. This prevents us from deleting items that are still needed in other contexts.
    //
    // In this sense, a context can be considered like a reference, and we are deleting objects
    // only if their reference count is 1 or 0. Otherwise we "decrement the count" by simply
    // removing that context.
    //
    // Basic algorithm: iterate through the list. If the given context is the only context
    // associated with an item, delete that item. Otherwise remove the context from that item.
    fun garbageCollectContext(keyContext : KeyContext)
}
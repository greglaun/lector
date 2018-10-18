package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.Deferred

interface Cache<Key : Any, Value : Any> {
    fun get(key: Key): Deferred<Value?>
    fun set(key: Key, value: Value): Deferred<Unit>
    fun compose(cache: Cache<Key, Value>) : Cache<Key, Value>
}

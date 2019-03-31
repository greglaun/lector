package com.greglaun.lector.data

import java.util.*

class LruCallbackList<T>(val size: Int = 4) {
    val ll = LinkedList<Pair<T, suspend (T) -> Unit>>()

    fun push(pair: Pair<T, suspend (T) -> Unit>) {
        if (ll.size >= size) {
            ll.removeFirst()
        }
        ll.add(pair)
    }

    fun get(key: T): (suspend (T) -> Unit)? {
        if (ll.isEmpty()) {
            return null
        }
        val matches = ll.filter { it.first == key}
        if (matches == null || matches.isEmpty()) {
            return null
        }
        return matches[0].second
    }
}
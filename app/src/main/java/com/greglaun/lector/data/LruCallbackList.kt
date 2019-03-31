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
        if (ll.size == 0) {
            return null
        }
        return ll.filter {
            it.first == key
        }.first()?.second
    }
}
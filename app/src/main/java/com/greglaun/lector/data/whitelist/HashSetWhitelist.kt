package com.greglaun.lector.data.whitelist

// A deterministic probabilistic set for testing
class HashSetWhitelist<T> : Whitelist<T> {
    val hashSet = HashSet<T>()
    override fun contains(element: T): Boolean {
        return hashSet.contains(element)
    }

    override fun add(element : T) {
        hashSet.add(element)
    }

    override fun delete(element: T) {
        hashSet.remove(element)
    }
}
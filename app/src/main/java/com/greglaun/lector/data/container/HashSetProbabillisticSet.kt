package com.greglaun.lector.data.container

// A deterministic probabilistic set for testing
class HashSetProbabillisticSet<T> : ProbabilisticSet<T> {
    val hashSet = HashSet<T>()
    override fun probablyContains(element: T): Boolean {
        return hashSet.contains(element)
    }

    override fun add(element : T) {
        hashSet.add(element)
    }

    override fun delete(element: T) {
        hashSet.remove(element)
    }
}
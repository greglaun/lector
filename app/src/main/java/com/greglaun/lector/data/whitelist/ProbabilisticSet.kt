package com.greglaun.lector.data.whitelist

interface ProbabilisticSet<T> {
    // Returns true if the set probably contains the element. A return value of false indicates
    // that the set definitely does not contain the element.
    fun probablyContains(element : T) : Boolean

    // Same as above but for collections.
    fun probablyContainsAll(elements : Collection<T>) : Boolean {
        for (e in elements) {
            if (!this.probablyContains(e)) {
                return false
            }
        }
        return true
    }

    fun add(element : T)
    fun addAll(elements : Collection<T>) {
        for (e in elements) {
            this.add(e)
        }
    }

    fun delete(element : T)
    fun deleteAll(elements : Collection<T>) {
        for (e in elements) {
            this.delete(e)
        }
    }
}
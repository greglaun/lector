package com.greglaun.lector.data.whitelist

interface Whitelist<T> {
    // Return true if the whitelist contains the element, and false otherwise
    fun contains(element : T) : Boolean

    // Same as above but for collections.
    fun containsAll(elements : Collection<T>) : Boolean {
        for (e in elements) {
            if (!this.contains(e)) {
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
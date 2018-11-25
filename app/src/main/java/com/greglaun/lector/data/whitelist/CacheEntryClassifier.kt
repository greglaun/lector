package com.greglaun.lector.data.whitelist

import kotlinx.coroutines.experimental.Deferred

interface CacheEntryClassifier<T> {
    fun contains(element : T): Deferred<Boolean>
    fun add(element: T): Deferred<Unit>
    fun delete(element: T): Deferred<Unit>
    fun iterator(): Iterator<T>
    fun update(from: T, to: T): Deferred<Unit>
    fun getAllTemporary(): Deferred<ListIterator<T>>
    fun markTemporary(element: T): Deferred<Unit>
    fun markPermanent(element: T): Deferred<Unit>
}
package com.greglaun.lector.data.whitelist

import kotlinx.coroutines.experimental.Deferred

interface Whitelist<T> {
    fun contains(element : T): Deferred<Boolean>
    fun add(element: T): Deferred<Unit>
    fun delete(element: T): Deferred<Unit>
    fun iterator(): Iterator<T>
}
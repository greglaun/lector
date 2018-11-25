package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.ArticleContext
import kotlinx.coroutines.experimental.Deferred

interface CacheEntryClassifier<T> {
    fun contains(element : T): Deferred<Boolean>
    fun add(element: T): Deferred<Unit>
    fun delete(element: T): Deferred<Unit>
    fun update(from: T, to: T): Deferred<Unit>
    fun getAllTemporary(): Deferred<List<ArticleContext>>
    fun getAllPermanent(): Deferred<List<ArticleContext>>
    fun markTemporary(element: T): Deferred<Unit>
    fun markPermanent(element: T): Deferred<Unit>
    fun isTemporary(element: T): Deferred<Boolean>
}
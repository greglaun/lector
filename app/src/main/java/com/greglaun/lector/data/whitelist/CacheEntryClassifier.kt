package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.ArticleContext
import kotlinx.coroutines.experimental.Deferred

interface CacheEntryClassifier<T> {
    // todo(cleanup): Move ArticleContext functions to another interface? Or re-architect?
    fun contains(element : T): Deferred<Boolean>
    fun add(element: T): Deferred<Long>
    fun delete(element: T): Deferred<Unit>
    fun update(from: T, to: T): Deferred<Unit>
    fun getAllTemporary(): Deferred<List<ArticleContext>>
    fun getAllPermanent(): Deferred<List<ArticleContext>>
    fun markTemporary(element: T): Deferred<Unit>
    fun markPermanent(element: T): Deferred<Unit>
    fun isTemporary(element: T): Deferred<Boolean>
    fun getArticleContext(context: String): Deferred<ArticleContext>
    fun updatePosition(currentRequestContext: T, position: T): Deferred<Unit>
}
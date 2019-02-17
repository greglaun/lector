package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.net.UnfinishedDownloadSource
import kotlinx.coroutines.experimental.Deferred

interface CacheEntryClassifier<T>: UnfinishedDownloadSource {
    // todo(cleanup): Move ArticleContext functions to another interface? Or re-architect?
    suspend fun contains(element : T): Boolean
    suspend fun add(element: T): Long
    suspend fun delete(element: T)
    fun update(from: T, to: T): Deferred<Unit>
    fun getAllTemporary(): Deferred<List<ArticleContext>>
    fun getAllPermanent(): Deferred<List<ArticleContext>>
    fun markTemporary(element: T): Deferred<Unit>
    fun markPermanent(element: T): Deferred<Unit>
    fun isTemporary(element: T): Deferred<Boolean>
    fun getArticleContext(context: String): Deferred<ArticleContext?>
    fun updatePosition(currentRequestContext: T, position: T): Deferred<Unit>
    fun getNextArticle(context: String): Deferred<ArticleContext?>
}
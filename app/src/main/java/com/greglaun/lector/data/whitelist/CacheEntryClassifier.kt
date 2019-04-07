package com.greglaun.lector.data.whitelist

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.net.UnfinishedDownloadSource

interface CacheEntryClassifier<T>: UnfinishedDownloadSource {
    // todo(cleanup): Move ArticleContext functions to another interface? Or re-architect?
    suspend fun contains(element : T): Boolean
    suspend fun add(element: T): Long
    suspend fun delete(element: T)
    suspend fun update(from: T, to: T)
    suspend fun getAllTemporary(): List<ArticleContext>
    suspend fun getAllPermanent(): List<ArticleContext>?
    suspend fun markTemporary(element: T)
    suspend fun markPermanent(element: T)
    suspend fun isTemporary(element: T): Boolean
    suspend fun getArticleContext(context: String): ArticleContext?
    suspend fun updatePosition(currentRequestContext: T, position: T)
    suspend fun getNextArticle(context: String): ArticleContext?
}
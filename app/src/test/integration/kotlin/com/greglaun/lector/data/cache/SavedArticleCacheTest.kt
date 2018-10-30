package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SavedArticleCacheTest {

    companion object {
            val testUrlString = "https://www.wikipedia.org/wiki/Dog"

            val savedArticleCache = HashMapSavedArticleCache()
            val responseSource = ResponseSourceFactory.createResponseSource(savedArticleCache,
                    File("testDir"))
    }

    @Test
    fun downloadAndCheckSaved() {
        val request = Request.Builder()
                .url(testUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null

        // Cache saved article cache should be empty
        runBlocking {
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertNull(cachedResponse)

        runBlocking {
            // Response from network
            networkResponse = responseSource.getWithContext(request, "Dog").await()
            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertTrue(networkResponse == cachedResponse)
    }

    @Test
    fun onlySetWhitelist() {
        val request = Request.Builder()
                .url(testUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null

        runBlocking {
            // Response from network
            networkResponse = responseSource.getWithContext(request, "Dog").await()
            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertTrue(networkResponse == cachedResponse)

        // Run garbage collection on a non-Dog context
        savedArticleCache.garbageCollectContext("Cat")
        runBlocking {
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        // Response should still be in cache
        assertTrue(networkResponse == cachedResponse)

        // Garbage collect Dog
        savedArticleCache.garbageCollectContext("Dog")

        // Cache saved article cache should be empty after Garbage Collection
        runBlocking {
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertNull(cachedResponse)
    }
}
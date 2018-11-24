package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SavedArticleCacheTest {

    companion object {
        val dogUrlString = "https://www.wikipedia.org/wiki/Dog"
        val catUrlString = "https://www.wikipedia.org/wiki/Cat"

        val savedArticleCache = HashMapSavedArticleCache()
        val testDir = File("testDir")
        val compositeCache = savedArticleCache.compose(
                NetworkCache(OkHttpConnectionFactory.createClient(testDir)))
        }

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
    }

    @Test
    fun downloadAndCheckSaved() {
        val request = Request.Builder()
                .url(dogUrlString)
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
            networkResponse = compositeCache.getWithContext(request, "Dog").await()

            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
            assertTrue(networkResponse!!.body()!!.string() == cachedResponse!!.body()!!.string())
        }
    }

    @Test
    fun onlySetWhitelist() {
        val dogRequest = Request.Builder()
                .url(dogUrlString)
                .build()
        val catRequest = Request.Builder()
                .url(catUrlString)
                .build()
        var networkDogResponse : Response? = null
        var cachedDogResponse : Response? = null
        var networkCatResponse : Response? = null
        var cachedCatResponse : Response? = null

        runBlocking {
            // Response from network
            networkDogResponse = compositeCache.getWithContext(dogRequest, "Dog").await()
            networkCatResponse = compositeCache.getWithContext(catRequest, "Dog").await()

            // Response is in cache now
            cachedCatResponse = savedArticleCache.getWithContext(catRequest, "Cat").await()
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog").await()
        }
        assertTrue(networkDogResponse == cachedDogResponse)
        assertTrue(networkCatResponse == cachedCatResponse)

        // Run garbage collection on a non-Dog articleContext
        savedArticleCache.garbageCollectContext("Cat")
        runBlocking {
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog").await()
        }
        // Response should still be in cache
        assertTrue(networkDogResponse == cachedDogResponse)

        // Garbage collect Dog
        savedArticleCache.garbageCollectContext("Dog")

        // Cache saved article cache should be empty after Garbage Collection
        runBlocking {
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog").await()
        }
        assertNull(cachedDogResponse)
    }
}
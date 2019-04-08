package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.net.TEN_GB
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SavedArticleCacheTest {

    companion object {
        const val dogUrlString = "https://www.wikipedia.org/wiki/Dog"
        const val catUrlString = "https://www.wikipedia.org/wiki/Cat"

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
        var networkResponse: Response?
        var cachedResponse : Response? = null

        // Cache saved article cache should be empty
        runBlocking {
            cachedResponse = savedArticleCache.getWithContext(request, "Dog")
        }
        assertNull(cachedResponse)

        runBlocking {
            // Response from network
            networkResponse = compositeCache.getWithContext(request, "Dog")

            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog")
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
            networkDogResponse = compositeCache.getWithContext(dogRequest, "Dog")
            networkCatResponse = compositeCache.getWithContext(catRequest, "Cat")

            // Response is in cache now
            cachedCatResponse = savedArticleCache.getWithContext(catRequest, "Cat")
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog")
        }
        assertTrue(networkDogResponse!!.peekBody(TEN_GB)!!.string() == cachedDogResponse!!.body()!!.string())
        assertTrue(networkCatResponse!!.body()!!.string() == cachedCatResponse!!.body()!!.string())

        runBlocking {
            // Run garbage collection on a non-Dog contextString
            savedArticleCache.garbageCollectContext("Cat")
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog")
        }
        // Response should still be in cache
        assertTrue(networkDogResponse!!.body()!!.string() ==  cachedDogResponse!!.body()!!.string())

        runBlocking {
            // Garbage collect Dog
            savedArticleCache.garbageCollectContext("Dog")

            // Cache saved article cache should be empty after Garbage Collection
            cachedDogResponse = savedArticleCache.getWithContext(dogRequest, "Dog")
        }
        assertNull(cachedDogResponse)
    }
}
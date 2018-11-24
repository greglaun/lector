package com.greglaun.lector.data.cache

import com.greglaun.lector.data.whitelist.HashSetWhitelist
import com.greglaun.lector.data.whitelist.Whitelist
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class ResponseSourceTest {
    var responseSource: ResponseSource? = null
    val testDir = File("testDir")
    val savedArticleCache = HashMapSavedArticleCache()

    val dogUrlString = "https://www.wikipedia.org/wiki/Dog"

    @Before
    fun setUp() {
        val whitelist: Whitelist<String> = HashSetWhitelist()
        responseSource = ResponseSource.createResponseSource(savedArticleCache, whitelist, testDir)
    }

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
    }

    @Test
    fun cacheMissWhenNotWhitelisted() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null
        runBlocking {
            // Response from network
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertNull(cachedResponse)
    }

    @Test
    fun cacheHitWhenWhitelisted() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null
        runBlocking {
            responseSource!!.add("Dog").await()
            // Response from network
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog").await()
        }
        assertTrue(networkResponse == cachedResponse)
    }

    @Test
    fun contains() {
        runBlocking {
            assertFalse(responseSource!!.contains("Dog").await())
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.contains("Dog").await())
        }
    }

    @Test
    fun delete() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.contains("Dog").await())
            responseSource!!.delete("Dog").await()
            assertFalse(responseSource!!.contains("Dog").await())
        }
    }
}
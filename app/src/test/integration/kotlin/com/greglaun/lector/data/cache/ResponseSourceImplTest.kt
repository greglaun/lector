package com.greglaun.lector.data.cache

import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import com.greglaun.lector.data.whitelist.HashSetCacheEntryClassifier
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class ResponseSourceImplTest {
    var responseSource: ResponseSourceImpl? = null
    val testDir = File("testDir")
    val savedArticleCache = HashMapSavedArticleCache()
    val testNetworkCache =  TestingNetworkCache(OkHttpConnectionFactory.createClient(testDir))

    val dogUrlString = "https://www.wikipedia.org/wiki/Dog"

    @Before
    fun setUp() {
        val cacheEntryClassifier: CacheEntryClassifier<String> = HashSetCacheEntryClassifier()
        responseSource = ResponseSourceImpl.createResponseSource(savedArticleCache,
                cacheEntryClassifier,
                testNetworkCache)
    }

    @After
    fun cleanup() {
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
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
        assertTrue(networkResponse!!.body()!!.string() == cachedResponse!!.body()!!.string())
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

    @Test
    fun setWithContext() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null
        runBlocking {
            responseSource!!.add("Dog").await()
            // Response from network
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            testNetworkCache.disableNetwork = true

            cachedResponse = responseSource!!.articleCache.
                    getWithContext(request!!, "Potato").await()
            assertNull(cachedResponse)

            responseSource!!.setWithContext(request, networkResponse!!,
                    "Potato")
            cachedResponse = responseSource!!.getWithContext(request!!, "Potato").await()
            assertTrue(networkResponse!!.body()!!.string() == cachedResponse!!.body()!!.string())
        }
    }

    @Test
    fun update() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.contains("Dog").await())
            responseSource!!.update("Dog", "Potato").await()
            assertFalse(responseSource!!.contains("Dog").await())
            assertTrue(responseSource!!.contains("Potato").await())
        }
    }

    @Test
    fun markTemporary() {
        runBlocking {
            assertEquals(responseSource!!.getAllTemporary().await().size, 0)
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.isTemporary("Dog").await())
            assertEquals(responseSource!!.getAllTemporary().await().size, 1)
            assertEquals(responseSource!!.getAllPermanent().await().size, 0)

            responseSource!!.markPermanent("Dog").await()
            assertFalse(responseSource!!.isTemporary("Dog").await())
            assertEquals(responseSource!!.getAllTemporary().await().size, 0)
            assertEquals(responseSource!!.getAllPermanent().await().size, 1)

            responseSource!!.markTemporary("Dog").await()
            assertTrue(responseSource!!.isTemporary("Dog").await())
            assertEquals(responseSource!!.getAllTemporary().await().size, 1)
            assertEquals(responseSource!!.getAllPermanent().await().size, 0)
        }
    }

    @Test
    fun garbageCollectTemporary() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse : Response? = null

        runBlocking {
            assertEquals(responseSource!!.getAllTemporary().await().size, 0)
            responseSource!!.add("Dog").await()
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNotNull(networkResponse)

            testNetworkCache.disableNetwork = true
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNotNull(networkResponse)

            responseSource!!.garbageCollectTemporary().await()
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNull(networkResponse)
        }
    }

    @Test
    fun garbageCollectContext() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse: Response? = null

        runBlocking {
            assertEquals(responseSource!!.getAllTemporary().await().size, 0)
            responseSource!!.add("Dog").await()
            responseSource!!.markPermanent("Dog")
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNotNull(networkResponse)

            testNetworkCache.disableNetwork = true
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNotNull(networkResponse)

            responseSource!!.garbageCollectContext("Dog").await()
            networkResponse = responseSource!!.getWithContext(request, "Dog").await()
            assertNull(networkResponse)
        }
    }

    @Test
    fun updatePosition() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertEquals(
                    responseSource!!.getArticleContext("Dog").await()!!.position, "")
            responseSource!!.updatePosition("Dog", "newPosition").await()
            assertEquals(
                    responseSource!!.getArticleContext("Dog").await()!!.position,
                    "newPosition")

        }
    }

    @Test
    fun getUnfinished() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertEquals(responseSource!!.getUnfinished().await().size, 1)
        }
    }

    @Test
    fun markFinished() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertEquals(responseSource!!.getUnfinished().await().size, 1)
            responseSource!!.markFinished("Dog").await()
            assertEquals(responseSource!!.getUnfinished().await().size, 0)
        }
    }
}
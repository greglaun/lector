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
            responseSource!!.add("Dog")
            // Response from network
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            // Response is in cache now
            cachedResponse = savedArticleCache.getWithContext(request, "Dog")
        }
        assertTrue(networkResponse!!.body()!!.string() == cachedResponse!!.body()!!.string())
    }

    @Test
    fun contains() {
        runBlocking {
            assertFalse(responseSource!!.contains("Dog"))
            responseSource!!.add("Dog")
            assertTrue(responseSource!!.contains("Dog"))
        }
    }

    @Test
    fun delete() {
        runBlocking {
            responseSource!!.add("Dog")
            assertTrue(responseSource!!.contains("Dog"))
            responseSource!!.delete("Dog")
            assertFalse(responseSource!!.contains("Dog"))
        }
    }

    @Test
    fun setWithContext() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse: Response?
        var cachedResponse : Response?
        runBlocking {
            responseSource!!.add("Dog")
            // Response from network
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            testNetworkCache.disableNetwork = true

            cachedResponse = responseSource!!.articleCache.
                    getWithContext(request!!, "Potato")
            assertNull(cachedResponse)

            responseSource!!.setWithContext(request, networkResponse!!,
                    "Potato")
            cachedResponse = responseSource!!.getWithContext(request!!, "Potato")
            assertTrue(networkResponse!!.body()!!.string() == cachedResponse!!.body()!!.string())
        }
    }

    @Test
    fun update() {
        runBlocking {
            responseSource!!.add("Dog")
            assertTrue(responseSource!!.contains("Dog"))
            responseSource!!.update("Dog", "Potato")
            assertFalse(responseSource!!.contains("Dog"))
            assertTrue(responseSource!!.contains("Potato"))
        }
    }

    @Test
    fun markTemporary() {
        runBlocking {
            assertEquals(responseSource!!.getAllTemporary().size, 0)
            responseSource!!.add("Dog")
            assertTrue(responseSource!!.isTemporary("Dog"))
            assertEquals(responseSource!!.getAllTemporary().size, 1)
            assertEquals(responseSource!!.getAllPermanent().size, 0)

            responseSource!!.markPermanent("Dog")
            assertFalse(responseSource!!.isTemporary("Dog"))
            assertEquals(responseSource!!.getAllTemporary().size, 0)
            assertEquals(responseSource!!.getAllPermanent().size, 1)

            responseSource!!.markTemporary("Dog")
            assertTrue(responseSource!!.isTemporary("Dog"))
            assertEquals(responseSource!!.getAllTemporary().size, 1)
            assertEquals(responseSource!!.getAllPermanent().size, 0)
        }
    }

    @Test
    fun garbageCollectTemporary() {
        val request = Request.Builder()
                .url(dogUrlString)
                .build()
        var networkResponse : Response? = null

        runBlocking {
            assertEquals(responseSource!!.getAllTemporary().size, 0)
            responseSource!!.add("Dog")
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            assertNotNull(networkResponse)

            testNetworkCache.disableNetwork = true
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            assertNotNull(networkResponse)

            responseSource!!.garbageCollectTemporary()
            networkResponse = responseSource!!.getWithContext(request, "Dog")
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
            assertEquals(responseSource!!.getAllTemporary().size, 0)
            responseSource!!.add("Dog")
            responseSource!!.markPermanent("Dog")
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            assertNotNull(networkResponse)

            testNetworkCache.disableNetwork = true
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            assertNotNull(networkResponse)

            responseSource!!.garbageCollectContext("Dog")
            networkResponse = responseSource!!.getWithContext(request, "Dog")
            assertNull(networkResponse)
        }
    }

    @Test
    fun updatePosition() {
        runBlocking {
            responseSource!!.add("Dog")
            assertEquals(
                    responseSource!!.getArticleContext("Dog")!!.position, "")
            responseSource!!.updatePosition("Dog", "newPosition")
            assertEquals(
                    responseSource!!.getArticleContext("Dog")!!.position,
                    "newPosition")
        }
    }

    @Test
    fun getUnfinished() {
        runBlocking {
            responseSource!!.add("Dog")
            assertEquals(responseSource!!.getUnfinished().await().size, 1)
        }
    }

    @Test
    fun markFinished() {
        runBlocking {
            responseSource!!.add("Dog")
            assertEquals(responseSource!!.getUnfinished().await().size, 1)
            responseSource!!.markFinished("Dog").await()
            assertEquals(responseSource!!.getUnfinished().await().size, 0)
        }
    }
}
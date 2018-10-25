package com.greglaun.lector.data.cache

import com.greglaun.lector.data.container.HashSetProbabillisticSet
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class SavedArticleCacheTest {

    companion object {
            val testUrlString = "https://www.wikipedia.org/wiki/Dog"

            val whiteList = HashSetProbabillisticSet<String>()
            val savedArticleCache = SavedArticleCache(
                    HashMapReferenceCountingWrapper(HashMapResponseCache()),
                    whiteList)
            val responseSource = ResponseSourceFactory.createResponseSource(savedArticleCache,
                    File("testDir"))

        @BeforeClass
        @JvmStatic
        fun setup() {
            whiteList.add(testUrlString)
        }
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
            cachedResponse = savedArticleCache.get(request).await()
        }
        assertNull(cachedResponse)

        runBlocking {
            networkResponse = responseSource.get(request).await() // Response from network
            cachedResponse = savedArticleCache.get(request).await() // Response is in cache now
        }
        assertTrue(networkResponse == cachedResponse)
    }

    @Test
    fun onlySetWhitelist() {
        val testBadUrlString = "https://www.wikipedia.org/wiki/Cat"

        val request = Request.Builder()
                .url(testBadUrlString)
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null

        runBlocking {
            networkResponse = responseSource.get(request).await() // Response from network
            cachedResponse = savedArticleCache.get(request).await() // Response is in cache now
        }
        assertTrue(networkResponse != cachedResponse)
        assertNull(cachedResponse)
    }
}
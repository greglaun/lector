package com.greglaun.lector.data.cache

import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SavedArticleCacheTest {

    @Test
    fun downloadAndCheckSaved() {
        val savedArticleCache = SavedArticleCache(HashMapReferenceCountingWrapper(HashMapResponseCache()))
        val responseSource = ResponseSourceFactory.createResponseSource(savedArticleCache,
                File("testDir"))
        val request = Request.Builder()
                .url("https://www.wikipedia.org/wiki/Dog")
                .build()
        var networkResponse : Response? = null
        var cachedResponse : Response? = null
        runBlocking {
             networkResponse = responseSource.get(request).await() // Response from network
        }
        runBlocking {
            cachedResponse = responseSource.get(request).await() // Response is in cache now
        }
        // todo(robustness): Fragile test: we may not want these to be equal
        assertTrue(networkResponse == cachedResponse)
    }
}
package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.greglaun.lector.data.cache.ResponseSourceImpl
import com.greglaun.lector.data.cache.SavedArticleCache
import com.greglaun.lector.data.whitelist.CacheEntryClassifier
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

// todo(testing): Merge with ResponseSourceTest.kt
class RoomResponseSourceTest {
    private var catRequest: Request? = null
    private var dogRequest: Request? = null
    var savedArticleCache: SavedArticleCache<Request, Response, String>? = null
    private var responseSource: ResponseSourceImpl? = null
    val testDir = File("testDir")

    val dogUrlString = "https://www.wikipedia.org/wiki/Dog"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, LectorDatabase::class.java).build()
        val cacheEntryClassifier: CacheEntryClassifier<String> = RoomCacheEntryClassifier(db)
        savedArticleCache = RoomSavedArticleCache(db)
        responseSource =  ResponseSourceImpl.createResponseSource(savedArticleCache!!, cacheEntryClassifier,
                testDir)

        dogRequest = Request.Builder()
                .url("https://www.wikipedia.org/wiki/Dog")
                .build()
        catRequest = Request.Builder()
                .url("https://www.wikipedia.org/wiki/Cat")
                .build()

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
            cachedResponse = savedArticleCache!!.getWithContext(request, "Dog").await()
        }
        assertNotNull(cachedResponse)
    }

    @Test
    fun contains() {
        runBlocking {
            assertFalse(responseSource!!.contains("Dog"))
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.contains("Dog"))
        }
    }

    @Test
    fun delete() {
        runBlocking {
            responseSource!!.add("Dog").await()
            assertTrue(responseSource!!.contains("Dog"))
            responseSource!!.delete("Dog").await()
            assertFalse(responseSource!!.contains("Dog"))
        }
    }
}
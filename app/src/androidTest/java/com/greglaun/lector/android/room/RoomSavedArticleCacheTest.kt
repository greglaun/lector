package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RoomSavedArticleCacheTest {
    private var db: LectorDatabase? = null
    private var cache: RoomSavedArticleCache? = null
    private var catRequest: Request? = null
    private var dogRequest: Request? = null
    val client = OkHttpClient()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        cache = RoomSavedArticleCache(db!!)

        dogRequest = Request.Builder()
                .url("https://www.wikipedia.org/wiki/Dog")
                .build()
        catRequest = Request.Builder()
                .url("https://www.wikipedia.org/wiki/Cat")
                .build()

    }

    @Test
    fun setContextUnprepared() {
        var context = "Potato"
        val catResponse = client.newCall(catRequest).execute()

        runBlocking {
            // This should not cause an error
            cache!!.setWithContext(catRequest!!, catResponse!!, context)
        }
    }

    @Test
    fun getWithContext() {
        var context = "Potato"
        val dogResponse = client.newCall(dogRequest).execute()
        val catResponse = client.newCall(catRequest).execute()

        runBlocking {

            cache!!.addContext(context)
            cache!!.setWithContext(catRequest!!, dogResponse!!, context)
            cache!!.setWithContext(dogRequest!!, catResponse!!, context)

            val result1 = cache!!.getWithContext(catRequest!!, context)
            assertNotNull(result1)

            val result2 = cache!!.getWithContext(dogRequest!!, context)
            assertNotNull(result2)
        }
    }

    @Test
    fun garbageCollectContext() {
        var context = "Potato"
        val dogResponse = client.newCall(dogRequest).execute()
        val catResponse = client.newCall(catRequest).execute()

        runBlocking {
            cache!!.addContext(context)
            cache!!.setWithContext(catRequest!!, dogResponse!!, context)
            cache!!.setWithContext(dogRequest!!, catResponse!!, context)

            cache!!.garbageCollectContext(context)

            val result1 = cache!!.getWithContext(catRequest!!, context)
            assertNull(result1)

            val result2 = cache!!.getWithContext(dogRequest!!, context)
            assertNull(result2)
        }
    }
}

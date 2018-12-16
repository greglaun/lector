package com.greglaun.lector.android.room

import android.arch.persistence.room.Room
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.experimental.runBlocking
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
            cache!!.setWithContext(catRequest!!, catResponse!!, context).await()
        }
    }

    @Test
    fun getWithContext() {
        var context = "Potato"
        val dogResponse = client.newCall(dogRequest).execute()
        val catResponse = client.newCall(catRequest).execute()

        runBlocking {

            cache!!.addContext(context).await()
            cache!!.setWithContext(catRequest!!, dogResponse!!, context).await()
            cache!!.setWithContext(dogRequest!!, catResponse!!, context).await()

            val result1 = cache!!.getWithContext(catRequest!!, context).await()
            assertNotNull(result1)

            val result2 = cache!!.getWithContext(dogRequest!!, context).await()
            assertNotNull(result2)
        }
    }

    @Test
    fun garbageCollectContext() {
        var context = "Potato"
        val dogResponse = client.newCall(dogRequest).execute()
        val catResponse = client.newCall(catRequest).execute()

        runBlocking {
            cache!!.addContext(context).await()
            cache!!.setWithContext(catRequest!!, dogResponse!!, context).await()
            cache!!.setWithContext(dogRequest!!, catResponse!!, context).await()

            cache!!.garbageCollectContext(context)

            val result1 = cache!!.getWithContext(catRequest!!, context).await()
            assertNull(result1)

            val result2 = cache!!.getWithContext(dogRequest!!, context).await()
            assertNull(result2)
        }
    }
}

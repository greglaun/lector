package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RoomCacheEntryClassifierTest {
    private var db: LectorDatabase? = null
    private var whitelist: RoomCacheEntryClassifier? = null


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        whitelist = RoomCacheEntryClassifier(db as LectorDatabase)
    }


    @Test
    fun contains() {
        runBlocking {
            assertFalse(whitelist!!.contains("a test string"))
        }
    }

    @Test
    fun add() {
        runBlocking {
            whitelist!!.add("Potato")
            assertTrue(whitelist!!.contains("Potato"))
        }
    }

    @Test
    fun delete() {
        runBlocking {
            whitelist!!.add("Radish")
            whitelist!!.delete("Radish").await()
            assertFalse(whitelist!!.contains("Radish"))
        }
    }

    @Test
    fun getDb() {
        assertThat(whitelist!!.db, equalTo(db))
    }
}
package com.greglaun.lector.android.room

import android.arch.persistence.room.Room
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class RoomWhitelistTest {
    private var db: ArticleCacheDatabase? = null
    private var whitelist: RoomWhitelist? = null


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, ArticleCacheDatabase::class.java).build()
        whitelist = RoomWhitelist(db as ArticleCacheDatabase)
    }


    @Test
    fun contains() {
        assertTrue(false)
    }

    @Test
    fun add() {
        assertTrue(false)
    }

    @Test
    fun delete() {
        assertTrue(false)
    }

    @Test
    fun getDb() {
        assertThat(whitelist!!.db, equalTo(db))
    }
}
package com.greglaun.lector.android.room

import android.arch.persistence.room.Room
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var articleContextDao: ArticleContextDao
    private var db: LectorDatabase? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        articleContextDao = db!!.articleContextDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndDeleteContext() {
        val bananaContext = RoomArticleContext(null,"Banana")
        articleContextDao.insert(bananaContext)
        val retrieved = articleContextDao.get("Banana")
        assertThat(retrieved.contextString, equalTo(bananaContext.contextString))
        articleContextDao.delete("Banana")
        val retrievedNull = articleContextDao.get("Banana")
        assertNull(retrievedNull)
    }

    @Test
    fun markTemporaryAndPermanent() {
        val apple = RoomArticleContext(null, "apple", "", true)
        val sauce = RoomArticleContext(null, "sauce", "", true)
        val pancakes = RoomArticleContext(null, "pancakes", "", true)
        val appleId = articleContextDao.insert(apple)
        val sauceId = articleContextDao.insert(sauce)
        val pancakeId = articleContextDao.insert(pancakes)

        articleContextDao.markPermanent("apple")
        articleContextDao.markPermanent("sauce")
        articleContextDao.markTemporary("sauce")

        apple.id = appleId
        apple.temporary = false
        sauce.id = sauceId
        pancakes.id = pancakeId

        val temporary = articleContextDao.getAllTemporary()
        val permanent = articleContextDao.getAllPermanent()
        assertTrue(permanent.size == 1)
        assertTrue(permanent.contains(apple))
        assertTrue(temporary.size == 2)
        assertTrue(temporary.contains(sauce))
        assertTrue(temporary.contains(pancakes))
    }
}
package com.greglaun.lector

import android.arch.persistence.room.Room
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greglaun.lector.android.data.ArticleCacheDatabase
import com.greglaun.lector.android.data.ArticleContext
import com.greglaun.lector.android.data.ArticleContextDao
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var articleContextDao: ArticleContextDao
    private var db: ArticleCacheDatabase? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, ArticleCacheDatabase::class.java).build()
        articleContextDao = db!!.articleContextDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val bananaContext = ArticleContext("Banana")
        articleContextDao.insert(bananaContext)
        val retrieved = articleContextDao.get("Banana")
        assertThat(retrieved, equalTo(bananaContext))
    }
}
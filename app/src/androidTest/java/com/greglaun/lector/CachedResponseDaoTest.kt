package com.greglaun.lector

import android.arch.persistence.room.Room
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greglaun.lector.android.data.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CachedResponseDaoTest {
    private lateinit var cachedResponseDao: CachedResponseDao
    private lateinit var articleContextDao: ArticleContextDao
    private var db: ArticleCacheDatabase? = null
    val serialResponse = "68747470733A2F2F656E2E77696B6970656469612E6F72672F726F626F74732E74787"
    val urlHash = "http://www.wikipedia.org/wiki/Test".hashCode().toString()
    val cachedResponse = CachedResponse(1, urlHash, serialResponse,
            "Banana")


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, ArticleCacheDatabase::class.java).build()
        cachedResponseDao = db!!.cachedResponseDao()
        articleContextDao = db!!.articleContextDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertForeignKeyMissing() {
        cachedResponseDao.insert(cachedResponse)
    }

    @Test
    fun insertForeignKeyPresent() {
        articleContextDao.insert(ArticleContext("Banana"))
        cachedResponseDao.insert(cachedResponse)
        val retrieved = cachedResponseDao.getWithContext("Banana")
        assertThat(retrieved.get(0), equalTo(cachedResponse))
    }
}
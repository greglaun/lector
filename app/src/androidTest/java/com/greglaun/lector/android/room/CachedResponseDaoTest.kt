package com.greglaun.lector.android.room

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    private var db: LectorDatabase? = null
    val serialResponse = "68747470733A2F2F656E2E77696B6970656469612E6F72672F726F626F74732E74787"
    val urlHash = "http://www.wikipedia.org/wiki/Test".hashCode().toString()
    var cachedResponse: CachedResponse? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
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
        cachedResponse = CachedResponse(1, urlHash, serialResponse,
                1L)
        cachedResponseDao.insert(cachedResponse!!)
    }

    @Test
    fun insertForeignKeyPresent() {
        articleContextDao.insert(RoomArticleContext(null, "Banana"))
        val bananaId = articleContextDao.get("Banana")!!.id
        cachedResponse = CachedResponse(1, urlHash, serialResponse,
                bananaId!!)
        cachedResponseDao.insert(cachedResponse!!)
        val retrieved = cachedResponseDao.getAllWithContext("Banana")
        assertThat(retrieved.get(0), equalTo(cachedResponse))
    }

    @Test
    fun insertListForeignKeyPresent() {
        articleContextDao.insert(RoomArticleContext(null, "Banana"))
        val bananaId = articleContextDao.get("Banana")!!.id
        cachedResponse = CachedResponse(1, urlHash, serialResponse,
                bananaId!!)
        val responses = ArrayList<CachedResponse>()
        for(i in 1..15) {
            responses.add(CachedResponse(i.toLong(), "A string" + i.toString(),
            "Another string" + i.toString(), bananaId))
        }
        cachedResponseDao.insert(responses)
        val retrieved = cachedResponseDao.getAllWithContext("Banana")
        assertThat(retrieved.size, equalTo(responses.size))
    }
}
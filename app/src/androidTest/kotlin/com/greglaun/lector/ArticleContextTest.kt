//package com.greglaun.lector
//
//import android.arch.persistence.room.Room
//import com.greglaun.lector.android.data.ArticleContext
//import com.greglaun.lector.android.data.ArticleContextDao
//import org.hamcrest.MatcherAssert.assertThat
//import org.hamcrest.core.IsEqual.equalTo
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import java.io.IOException
//
//@RunWith(AndroidJUnit4::class)
//class SimpleEntityReadWriteTest {
//    private lateinit var articleContextDao: ArticleContextDao
//    private lateinit var db: TestDatabase
//
//    @Before
//    fun createDb() {
//        val context = ApplicationProvider.getApplicationContext()
//        db = Room.inMemoryDatabaseBuilder(
//                context, TestDatabase::class.java).build()
//        articleContextDao = db.getArticleContextDao()
//    }
//
//    @After
//    @Throws(IOException::class)
//    fun closeDb() {
//        db.close()
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun writeUserAndReadInList() {
//        val bananaContext = ArticleContext("Banana")
//        articleContextDao.insert(bananaContext)
//        val retrieved = articleContextDao.get("Banana")
//        assertThat(retrieved, equalTo(bananaContext))
//    }
//}
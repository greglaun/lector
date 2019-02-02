package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CourseArticleJoinDaoTest {
    private lateinit var courseArticleJoinDao: CourseArticleJoinDao
    private var db: LectorDatabase? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        courseArticleJoinDao = db!!.courseArticleJoinDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    fun getArticlesWithCourseId() {
        assertTrue(false)
    }

    @Test
    fun getMaxOccupiedPosition() {
        assertTrue(false)
    }

    @Test
    fun getLeastOccupiedPosition() {
        assertTrue(false)
    }

    @Test
    fun insert() {
        assertTrue(false)
    }

    @Test
    fun get() {
        assertTrue(false)
    }

    @Test
    fun getNextInCourse() {
        assertTrue(false)
    }
}
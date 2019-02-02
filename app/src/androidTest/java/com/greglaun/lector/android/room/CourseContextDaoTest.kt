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
class CourseContextDaoTest {
    private lateinit var courseContextDao: CourseContextDao
    private var db: LectorDatabase? = null

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        courseContextDao = db!!.courseContextDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    fun getAll() {
        assertTrue(false)
    }

    @Test
    fun get() {
        assertTrue(false)
    }

    @Test
    fun delete() {
        assertTrue(false)
    }

    @Test
    fun insert() {
        assertTrue(false)
    }

    @Test
    fun updatecourseContext() {
        assertTrue(false)
    }

    @Test
    fun deleteAll() {
        assertTrue(false)
    }

    @Test
    fun updatePosition() {
        assertTrue(false)
    }
}
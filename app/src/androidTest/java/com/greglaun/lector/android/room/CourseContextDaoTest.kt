package com.greglaun.lector.android.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
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
    fun delete() {
        courseContextDao.insert(RoomCourseContext(null, "Fruit"))
        assertNotNull(courseContextDao.get("Fruit"))
        courseContextDao.delete("Fruit")
        assertNull(courseContextDao.get("Fruit"))
    }

    @Test
    fun updatecourseContext() {
        courseContextDao.insert(RoomCourseContext(null, "Fruit"))
    }

    @Test
    fun deleteAll() {
        courseContextDao.insert(RoomCourseContext(null, "Fruit"))
        courseContextDao.insert(RoomCourseContext(null, "Bruit"))
        courseContextDao.insert(RoomCourseContext(null, "Fluit"))
        courseContextDao.insert(RoomCourseContext(null, "Suit"))
        assertEquals(courseContextDao.getAll().size, 4)

        courseContextDao.deleteAll()
        assertEquals(courseContextDao.getAll().size, 0)
    }

    @Test
    fun updatePosition() {
        courseContextDao.insert(RoomCourseContext(null, "Fruit", 12))
        assertEquals(courseContextDao.get("Fruit").position, 12)
        courseContextDao.updatePosition("Fruit", 21)
        assertEquals(courseContextDao.get("Fruit").position, 21)
    }
}
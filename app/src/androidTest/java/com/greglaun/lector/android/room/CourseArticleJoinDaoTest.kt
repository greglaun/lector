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
class CourseArticleJoinDaoTest {
    private lateinit var articleContextDao: ArticleContextDao
    private lateinit var courseContextDao: CourseContextDao
    private lateinit var courseArticleJoinDao: CourseArticleJoinDao

    private var db: LectorDatabase? = null

    @Before
    fun createAndPrepareDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, LectorDatabase::class.java).build()
        courseContextDao = db!!.courseContextDao()
        articleContextDao = db!!.articleContextDao()
        courseArticleJoinDao = db!!.courseArticleJoinDao()
        prepareDb()
    }

    private fun prepareDb() {
        articleContextDao.insert(RoomArticleContext(null,"Apple",
                "", false, true))
        articleContextDao.insert(RoomArticleContext(null,"Banana",
                "", false, true))
        articleContextDao.insert(RoomArticleContext(null,"Pear",
                "", false, true))
        courseContextDao.insert(RoomCourseContext(null, "Fruit"))

        courseArticleJoinDao.insert(CourseArticleJoin(1, 1, -1))
        courseArticleJoinDao.insert(CourseArticleJoin(1, 2, 0))
        courseArticleJoinDao.insert(CourseArticleJoin(1, 3, 1))
    }


    @After
    @Throws(IOException::class)
    fun closeDb() {
        db?.close()
    }

    @Test
    fun getArticlesWithCourseId() {
        val fruitArticles = courseArticleJoinDao.getArticlesWithCourseId(1)
        assertTrue(fruitArticles.contains(RoomArticleContext(1, "Apple",
                "", false, true)))
        assertTrue(fruitArticles.contains(RoomArticleContext(2, "Banana",
                "", false, true)))
        assertTrue(fruitArticles.contains(RoomArticleContext(3, "Pear",
                        "", false, true)))
    }

    @Test
    fun getMaxOccupiedPosition() {
        val maxPosition = courseArticleJoinDao.getMaxOccupiedPosition(1)
        assertEquals(maxPosition, 1L)
    }

    @Test
    fun getLeastOccupiedPosition() {
        val leastPosition = courseArticleJoinDao.getLeastOccupiedPosition(1)
        assertEquals(leastPosition, -1L)
    }

    @Test
    fun getNextInCourse() {
        assertEquals(courseArticleJoinDao.getNextInCourse(1, -1),
                RoomArticleContext(1, "Apple", "", false,
                        true))
        assertEquals(courseArticleJoinDao.getNextInCourse(1, 0),
                RoomArticleContext(1, "Apple", "", false,
                        true))
        assertEquals(courseArticleJoinDao.getNextInCourse(1, 1),
                RoomArticleContext(2, "Banana", "", false,
                        true))
        assertEquals(courseArticleJoinDao.getNextInCourse(1, 2),
                RoomArticleContext(3, "Pear", "", false,
                        true))
        assertNull(courseArticleJoinDao.getNextInCourse(1, 3))
    }
}
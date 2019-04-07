package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.ThinCourseDetails

class RoomCourseSource(var db: LectorDatabase) : CourseSource {
    override suspend fun getCourses(): List<CourseContext>? {
        return db.courseContextDao().getAll()
    }

    override suspend fun getArticlesForCourse(courseId: Long): List<ArticleContext>? {
        return db.courseArticleJoinDao().getArticlesWithCourseId(courseId)
    }

    override suspend fun getNextInCourse(courseName: String,
                                         articleName: String): ArticleContext? {
        val courseContext = db.courseContextDao().get(courseName)
        if (courseContext == null) {
            return null
        }
        val articleContext = db.articleContextDao().get(articleName)
        if (articleContext == null) {
            return null
        }
        return db.courseArticleJoinDao().getNextInCourse(courseContext.id!!, articleContext.id!!)
    }

    override suspend fun addArticleForSource(courseName: String, articleName: String) {
        var articleContext: ArticleContext? = db.articleContextDao().get(articleName)
        if (articleContext == null) {
            val articleId = db.articleContextDao().insert(
                    RoomArticleContext(null, articleName, "", false))
            articleContext =
                    RoomArticleContext(articleId, articleName, "", false)
        }
        val courseContext = db.courseContextDao().get(courseName)
        val maxOccupied = db.courseArticleJoinDao().getMaxOccupiedPosition(
                    courseContext!!.id!!) ?: -1L
        val courseArticleJoin = CourseArticleJoin(courseContext!!.id!!,
                articleContext.id!!, maxOccupied + 1)
        val existingEntry = db.courseArticleJoinDao().get(courseContext.id!!,
                articleContext.id!!)
        if (existingEntry == null) {
            db.courseArticleJoinDao().insert(courseArticleJoin)
        }
    }

    override suspend fun delete(courseName: String) {
        db.courseContextDao().delete(courseName)
    }

    override suspend fun add(courseContext: CourseContext): Long {
        val existingEntry = db.courseContextDao().get(courseContext.courseName)
        if (existingEntry == null) {
            val roomCourse = RoomCourseContext(null, courseName = courseContext.courseName,
                    position = courseContext.position)
            val newId = db.courseContextDao().insert(roomCourse)
            return newId
        } else {
            return existingEntry.id!!
        }
    }

    override suspend fun addCourseDetails(courseDetails: ThinCourseDetails) {
        add(RoomCourseContext(null, courseDetails.name))
        courseDetails.articleNames.forEach {
            addArticleForSource(courseDetails.name, urlToContext(it))
        }
    }
}
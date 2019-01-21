package com.greglaun.lector.android.room

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.urlToContext
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.ThinCourseDetails
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async

class RoomCourseSource(var db: LectorDatabase) : CourseSource {
    override fun getCourses(): Deferred<List<CourseContext>> {
        return GlobalScope.async{
            db.courseContextDao().getAll()
        }
    }

    override fun getArticlesForCourse(courseId: Long): Deferred<List<ArticleContext>> {
        return GlobalScope.async {
            db.courseArticleJoinDao().getArticlesWithCourseId(courseId)
        }
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

    override fun addArticleForSource(courseName: String, articleName: String): Deferred<Unit> {
        return GlobalScope.async {
            var articleContext: ArticleContext? = db.articleContextDao().get(articleName)
            if (articleContext == null) {
                val articleId = db.articleContextDao().insert(
                        RoomArticleContext(null, articleName, "", false))
                articleContext =
                        RoomArticleContext(articleId, articleName, "", false)
            }
            val courseContext = db.courseContextDao().get(courseName)
            val maxOccupied = db.courseArticleJoinDao().getMaxOccupiedPosition(
                    courseContext.id!!) ?: -1L
            val courseArticleJoin = CourseArticleJoin(courseContext.id!!,
                    articleContext.id!!, maxOccupied + 1)
            val existingEntry = db.courseArticleJoinDao().get(courseContext.id!!,
                    articleContext.id!!)
            if (existingEntry == null) {
                db.courseArticleJoinDao().insert(courseArticleJoin)
            }
            Unit
        }
    }

    override fun delete(courseName: String): Deferred<Unit> {
        return GlobalScope.async {
            db.courseContextDao().delete(courseName)
        }
    }

    override fun add(courseContext: CourseContext): Deferred<Long> {
        return GlobalScope.async {
            val existingEntry = db.courseContextDao().get(courseContext.courseName)
            if (existingEntry == null) {
                val roomCourse = RoomCourseContext(null, courseName = courseContext.courseName,
                        position = courseContext.position)
                val newId = db.courseContextDao().insert(roomCourse)
                newId
            } else {
                existingEntry.id!!
            }
        }
    }

    override fun addCourseDetails(courseDetails: ThinCourseDetails): Deferred<Unit> {
        return GlobalScope.async {
            add(RoomCourseContext(null, courseDetails.name)).await()
            courseDetails.articleNames.forEach {
                addArticleForSource(courseDetails.name, urlToContext(it))
            }
        }
    }
}
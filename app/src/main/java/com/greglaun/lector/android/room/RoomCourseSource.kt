package com.greglaun.lector.android.room

import com.greglaun.lector.data.CourseSource
import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.CourseContext
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
}
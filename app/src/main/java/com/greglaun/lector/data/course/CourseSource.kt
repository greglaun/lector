package com.greglaun.lector.data.course

import com.greglaun.lector.data.cache.ArticleContext
import kotlinx.coroutines.experimental.Deferred

interface CourseSource {
    fun getCourses(): Deferred<List<CourseContext>>
    fun getArticlesForCourse(courseId: Long): Deferred<List<ArticleContext>>
    fun delete(courseName: String): Deferred<Unit>
    fun add(courseContext: CourseContext): Deferred<Long>
    fun addArticleForSource(courseName: String, articleName: String): Deferred<Unit>
}
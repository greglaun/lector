package com.greglaun.lector.data

import com.greglaun.lector.data.cache.ArticleContext
import com.greglaun.lector.data.cache.CourseContext
import kotlinx.coroutines.experimental.Deferred

interface CourseSource {
    fun getCourses(): Deferred<List<CourseContext>>
    fun getArticlesForCourse(courseId: Long): Deferred<List<ArticleContext>>
}
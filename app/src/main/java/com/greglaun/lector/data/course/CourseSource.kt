package com.greglaun.lector.data.course

import com.greglaun.lector.data.cache.ArticleContext

interface CourseSource {
    suspend fun getCourses(): List<CourseContext>?
    suspend fun getArticlesForCourse(courseId: Long): List<ArticleContext>?
    suspend fun delete(courseName: String)
    suspend fun add(courseContext: CourseContext): Long
    suspend fun addArticleForSource(courseName: String, articleName: String)
    suspend fun addCourseDetails(courseDetails: ThinCourseDetails)
    suspend fun getNextInCourse(courseName: String, articleName: String): ArticleContext?
}
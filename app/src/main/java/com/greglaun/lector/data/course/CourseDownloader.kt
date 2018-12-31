package com.greglaun.lector.data.course

import kotlinx.coroutines.experimental.Deferred

interface CourseDownloader {
    fun downloadAllCourses(): Deferred<List<CourseContext>>
    fun downloadCourses(queryString: String): Deferred<List<CourseContext>>
    fun fetchCourseDetails(courseContexts: List<CourseContext>):
            Deferred<Map<CourseContext, CourseDetails>>
}
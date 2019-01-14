package com.greglaun.lector.data.course

import kotlinx.coroutines.experimental.Deferred

interface CourseDownloader {
    // todo(REST): Add ability to send a query string
    fun downloadCourseMetadata(): Deferred<List<CourseMetadata>?>
    fun fetchCourseDetails(courseNames: List<String>):
            Deferred<Map<String, CourseDetails>?>
    fun fetchCourseDetails(courseMetadata: CourseMetadata):  Deferred<CourseDetails?>
}
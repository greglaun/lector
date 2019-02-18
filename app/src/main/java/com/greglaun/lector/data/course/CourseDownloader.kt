package com.greglaun.lector.data.course

interface CourseDownloader {
    // todo(REST): Add ability to send a query string
    suspend fun downloadCourseMetadata(): List<CourseMetadata>?
    suspend fun fetchCourseDetails(courseNames: List<String>): Map<String, ThinCourseDetails>?
    suspend fun fetchCourseDetails(courseMetadata: CourseMetadata): ThinCourseDetails?
}
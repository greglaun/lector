package com.greglaun.lector.data.course

import android.util.Log
import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.net.TEN_GB
import okhttp3.Request
import java.io.File



class CourseDownloaderImpl(private val baseUrl: String, lruCacheDir: File):
        CourseDownloader{
    private val client = OkHttpConnectionFactory.createClient(lruCacheDir)


    override suspend fun downloadCourseMetadata(): List<CourseMetadata>? {
        val responseString = downloadCourseInfo()
        return if (responseString != null) {
            extractCourseMetadata(responseString)
        } else {
            null
        }
    }

    override suspend fun fetchCourseDetails(courseNames: List<String>):
            Map<String, ThinCourseDetails>? {
        val responseString = downloadCourseInfo()
        return if (responseString != null) {
            toCourseDetailsMap(extractCourseMap(responseString))
        } else {
            null
        }
    }

    private fun downloadCourseInfo(): String? {
        val request = Request.Builder().url("$baseUrl/courses/")
                .build()
        val response = client.newCall(request).execute() ?: return null
        if (!response.isSuccessful) {
            Log.i("CourseDownloaderImpl", "Fetching courses failed: "
                    + response.code().toString())
            return null
        }
        // Everything fits into memory at this stage of things
        return response.peekBody(TEN_GB).string()
    }

    override suspend fun fetchCourseDetails(courseMetadata: CourseMetadata): ThinCourseDetails? {
        fetchCourseDetails(listOf(courseMetadata.name))?.let {
            return it[courseMetadata.name]
        }
        return null
    }
}
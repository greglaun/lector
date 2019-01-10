package com.greglaun.lector.data.course

import android.util.Log
import com.greglaun.lector.data.net.OkHttpConnectionFactory
import com.greglaun.lector.data.net.TEN_GB
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import okhttp3.Request
import java.io.File



class CourseDownloaderImpl(val baseUrl: String, lruCacheDir: File):
        CourseDownloader{
    private val client = OkHttpConnectionFactory.createClient(lruCacheDir)


    override fun downloadCourseMetadata(): Deferred<List<CourseMetadata>?> {
        return GlobalScope.async fetch@{
            val responseString = downloadCourseInfo()
            if (responseString != null) {
                 extractCourseMetadata(responseString!!)
             } else {
                null
            }
        }
    }

    override fun fetchCourseDetails(courseNames: List<String>):
            Deferred<Map<String, CourseDetails>?> {
        return GlobalScope.async fetch@{
            val responseString = downloadCourseInfo()
            if (responseString != null) {
                val detailMap = toCourseDetailsMap(extractCourseMap(responseString!!))
                detailMap
            } else {
                null
            }
        }
    }

    private fun downloadCourseInfo(): String? {
        val request = Request.Builder().url(baseUrl + "/courses/")
                .build()
        val response = client.newCall(request).execute()
        if (response == null) {
            return null
        }
        if (!response.isSuccessful) {
            Log.i("CourseDownloaderImpl", "Fetching courses failed: " + response.code().toString())
            return null
        }
        // Everything fits into memory at this stage of things
        return response.peekBody(TEN_GB).string()
    }

    override fun fetchCourseDetails(courseMetadata: CourseMetadata): Deferred<CourseDetails?> {
        return GlobalScope.async {
            fetchCourseDetails(listOf(courseMetadata.name)).await()?.let {
                it[courseMetadata.name]
            }
        }
    }
}
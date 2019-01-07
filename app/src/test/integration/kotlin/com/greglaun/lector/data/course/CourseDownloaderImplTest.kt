package com.greglaun.lector.data.course

import com.greglaun.lector.BuildConfig
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.io.File

class CourseDownloaderImplTest {
    val testDir = File("testDir")
    val courseDownloader = CourseDownloaderImpl(BuildConfig.BASE_URL, testDir)

    @Test
    fun downloadAllCourseNames() {
        runBlocking {
            val result = courseDownloader.downloadAllCourseNames().await()
            assertTrue(result!!.contains("Ice Cream"))
            assertTrue(result!!.contains("Furry Friends"))
        }
    }

    @Test
    fun fetchCourseDetails() {
        runBlocking {
            val detailsMap = courseDownloader.fetchCourseDetails(
                    listOf("Ice Cream", "Furry Friends")).await()
            assertTrue(detailsMap!!.keys.size > 0)
            assertTrue(detailsMap!!.containsKey("Ice Cream"))
            assertTrue(detailsMap!!.containsKey("Furry Friends"))
            assertTrue(detailsMap!!.get("Ice Cream")!!.
                    articleNames.contains("https://en.wikipedia.org/wiki/Cold-stimulus_headache"))
        }
    }
}
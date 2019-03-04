package com.greglaun.lector.data.course

import com.greglaun.lector.BuildConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CourseDownloaderImplTest {
    val testDir = File("testDir")
    val courseDownloader = CourseDownloaderImpl(BuildConfig.BASE_URL_LOCAL, testDir)

    @Test
    fun downloadAllCourseNames() {
        runBlocking {
            val result = courseDownloader.downloadCourseMetadata()
            assertTrue(result!!.contains(CourseMetadata("Ice Cream")))
            assertTrue(result!!.contains(CourseMetadata("Furry Friends")))
        }
    }

    @Test
    fun fetchCourseDetails() {
        runBlocking {
            val detailsMap = courseDownloader.fetchCourseDetails(
                    listOf("Ice Cream", "Furry Friends"))
            assertTrue(detailsMap!!.keys.size > 0)
            assertTrue(detailsMap!!.containsKey("Ice Cream"))
            assertTrue(detailsMap!!.containsKey("Furry Friends"))
            assertTrue(detailsMap!!.get("Ice Cream")!!.
                    articleNames.contains("https://en.wikipedia.org/wiki/Cold-stimulus_headache"))
        }
    }
}
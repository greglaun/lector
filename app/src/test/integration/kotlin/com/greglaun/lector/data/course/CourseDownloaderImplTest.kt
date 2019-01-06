package com.greglaun.lector.data.course

import com.greglaun.lector.BuildConfig
import org.junit.Test
import java.io.File

class CourseDownloaderImplTest {
    val testDir = File("testDir")
    val courseDownloader = CourseDownloaderImpl(BuildConfig.BASE_URL, testDir)

    @Test
    fun downloadAllCourseNames() {
    }

    @Test
    fun fetchCourseDetails() {
    }
}
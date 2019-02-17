package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.CourseSource
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.mockito.Mockito.*

class CourseBrowserPresenterTest {
    val mockView = mock(CourseBrowserContract.View::class.java)
    val mockDownloader = mock(CourseDownloader::class.java)
    val mockSource = mock(CourseSource::class.java)
    val courseBrowserPresenter = CourseBrowserPresenter(mockView, mockDownloader, mockSource)

    @Test
    fun beginCourseDownload() {
        runBlocking {
            courseBrowserPresenter.beginCourseDownload()
        }
        verify(mockDownloader, times(1)).downloadCourseMetadata()
    }

    @Test
    fun onCourseDetailSelected() {
        runBlocking {
                courseBrowserPresenter.onCourseDetailSelected(CourseMetadata("A Course"))
            }
            verify(mockDownloader, times(1)).
                    fetchCourseDetails(CourseMetadata("A Course"))
    }

    @Test
    fun onCoursesSaved() {
        val courseDetails = listOf(CourseMetadata("One"),
                CourseMetadata("Two"), CourseMetadata("Three"))
        runBlocking {
            val job = courseBrowserPresenter.onCoursesSaved(courseDetails)
            verify(mockDownloader, times(1)).
                    fetchCourseDetails(CourseMetadata("One"))
        }
    }
}
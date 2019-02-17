package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.ThinCourseDetails
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

class CourseBrowserPresenter(val view: CourseBrowserContract.View,
                             val courseDownloader: CourseDownloader,
                             val courseSource: CourseSource)
    : CourseBrowserContract.Presenter {
    override val courseMetadatalist = mutableListOf<CourseMetadata>()
    var currentDetails: ThinCourseDetails? = null

    override fun onAttach() {}

    override fun onDetach() {}

    override fun getLectorView(): CourseBrowserContract.View? {
        return view
    }

    override suspend fun beginCourseDownload() {
        courseDownloader.downloadCourseMetadata()?.let {
            it.await()?.let {
                courseMetadatalist.clear()
                courseMetadatalist.addAll(it)
                view.onCourseListChanged()
                view.showCourses(it)
            }
        }
    }

    override fun onSaveDetailsPressed() {
        currentDetails?.let {
            onCourseSaved(it)
        }
    }

    override suspend fun onCourseDetailSelected(courseMetadata: CourseMetadata) {
        courseDownloader.fetchCourseDetails(courseMetadata)?.let {
            it.await()?.let {
                currentDetails = it
                view.showCourseDetails(it)
            }
        }
    }

    private fun onCourseSaved(courseDetails: ThinCourseDetails) {
        GlobalScope.launch {
            courseSource.addCourseDetails(courseDetails).await()
            view.showToast("Course " + courseDetails.name + " added.")
        }
    }

    override suspend fun onCoursesSaved(courseMetadata: List<CourseMetadata>) {
            courseMetadata.forEach {
                courseDownloader.fetchCourseDetails(it)?.also {
                    it.await()?.let {
                        courseSource.addCourseDetails(it).await()
                }
            }
        }
    }
}
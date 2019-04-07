package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.ThinCourseDetails
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseBrowserPresenter(val view: CourseBrowserContract.View,
                             private val courseDownloader: CourseDownloader,
                             private val courseSource: CourseSource)
    : CourseBrowserContract.Presenter {
    override val courseMetadatalist = mutableListOf<CourseMetadata>()
    private var currentDetails: ThinCourseDetails? = null

    override fun onAttach() {}

    override fun onDetach() {}

    override fun getLectorView(): CourseBrowserContract.View? {
        return view
    }

    override suspend fun beginCourseDownload() {
        courseDownloader.downloadCourseMetadata()?.let {
            courseMetadatalist.clear()
            courseMetadatalist.addAll(it)
            view.onCourseListChanged()
            view.showCourses(it)
        }
    }

    override fun onSaveDetailsPressed() {
        currentDetails?.let {
            onCourseSaved(it)
        }
    }

    override suspend fun onCourseDetailSelected(courseMetadata: CourseMetadata) {
        courseDownloader.fetchCourseDetails(courseMetadata)?.let {
            currentDetails = it
            view.showCourseDetails(it)
        }
    }

    private fun onCourseSaved(courseDetails: ThinCourseDetails) {
        GlobalScope.launch {
            courseSource.addCourseDetails(courseDetails)
            view.showToast("Course " + courseDetails.name + " added.")
        }
    }

    override suspend fun onCoursesSaved(courseMetadata: List<CourseMetadata>) {
            courseMetadata.forEach { metadata ->
                courseDownloader.fetchCourseDetails(metadata)?.also {
                    courseSource.addCourseDetails(it)
            }
        }
    }
}
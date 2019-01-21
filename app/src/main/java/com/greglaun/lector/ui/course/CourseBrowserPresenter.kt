package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.ThinCourseDetails
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.CourseSource
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

    override fun beginCourseDownload() {
        GlobalScope.launch {
            courseDownloader.downloadCourseMetadata().await()?.also {
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

    override fun onCourseDetailSelected(courseMetadata: CourseMetadata) {
        GlobalScope.launch {
            courseDownloader.fetchCourseDetails(courseMetadata).await()?.also {
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

    override fun onCoursesSaved(courseMetadata: List<CourseMetadata>) {
        GlobalScope.launch {
            courseMetadata.forEach {
                courseDownloader.fetchCourseDetails(it).await()?.also {
                    courseSource.addCourseDetails(it).await()
                }
            }
        }
    }
}
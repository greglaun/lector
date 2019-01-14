package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDetails
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView

interface CourseBrowserContract {
    interface View : LectorView {
        fun showCourses(courses: List<CourseMetadata>)
        fun showCourseDetails(courseDetails: CourseDetails)
        fun onCourseListChanged()
        fun showToast(message: String)
    }

    interface Presenter : LectorPresenter<View> {
        val courseMetadatalist: List<CourseMetadata>
        fun beginCourseDownload()
        fun onCourseDetailSelected(courseMetadata: CourseMetadata)
        fun onCoursesSaved(courseMetadata: List<CourseMetadata>)
        fun onSaveDetailsPressed()
    }
}
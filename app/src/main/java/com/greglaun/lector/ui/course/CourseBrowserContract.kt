package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.ThinCourseDetails
import com.greglaun.lector.ui.base.LectorPresenter
import com.greglaun.lector.ui.base.LectorView

interface CourseBrowserContract {
    interface View : LectorView {
        fun showCourses(courses: List<CourseMetadata>)
        fun showCourseDetails(courseDetails: ThinCourseDetails)
        fun onCourseListChanged()
        fun showToast(message: String)
        fun navigateCurrentArticle()
    }

    interface Presenter : LectorPresenter<View> {
        val courseMetadatalist: List<CourseMetadata>
        suspend fun beginCourseDownload()
        suspend fun onCourseDetailSelected(courseMetadata: CourseMetadata)
        suspend fun onCoursesSaved(courseMetadata: List<CourseMetadata>)
        fun onSaveDetailsPressed()
    }
}
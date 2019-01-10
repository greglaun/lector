package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDownloader
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

class CourseBrowserPresenter(val view: CourseBrowserContract.View,
                             val courseDownloader: CourseDownloader)
    : CourseBrowserContract.Presenter {

    override fun onAttach() {}

    override fun onDetach() {}

    override fun getLectorView(): CourseBrowserContract.View? {
        return view
    }

    override fun beginCourseDownload() {
        GlobalScope.launch {
            courseDownloader.downloadCourseMetadata().await()?.also {
                view.showCourses(it)
            }
        }
    }

    override fun onCourseDetailSelected(courseName: String) {
        GlobalScope.launch {
            courseDownloader.downloadCourseMetadata()
        }
    }

    override fun onCourseSaved(courseName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
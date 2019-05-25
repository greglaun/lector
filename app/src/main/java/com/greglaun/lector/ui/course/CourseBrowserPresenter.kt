package com.greglaun.lector.ui.course

import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseMetadata
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.data.course.ThinCourseDetails
import com.greglaun.lector.store.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseBrowserPresenter(val view: CourseBrowserContract.View,
                             private val courseDownloader: CourseDownloader,
                             private val courseSource: CourseSource,
                             private val store: Store)
    : CourseBrowserContract.Presenter , StateHandler {

    override val courseMetadatalist = mutableListOf<CourseMetadata>()
    private var currentDetails: ThinCourseDetails? = null
    private var isActivityRunning = false

    override fun onAttach() {
        isActivityRunning = true
        store.stateHandlers.add(this)
    }

    override fun onDetach() {
        isActivityRunning = false
        store.stateHandlers.remove(this)
    }

    override fun getLectorView(): CourseBrowserContract.View? {
        return view
    }

    override suspend fun handle(state: State) {
        when(state.navigation) {
            Navigation.CURRENT_ARTICLE -> {
                view.navigateCurrentArticle()
            }
        }
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
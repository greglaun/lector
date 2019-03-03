package com.greglaun.lector.store.sideeffect.fetch

import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.UpdateAction

suspend fun fetchCourseDetails(action: ReadAction.FetchCourseDetailsAction,
                               courseDownloader: CourseDownloader,
                               actionDispatcher: suspend (Action) -> Unit) {
    val courseName = action.courseContext.courseName
    val detailsMap = courseDownloader.fetchCourseDetails(listOf(courseName))
    detailsMap?.let {
        if (detailsMap.containsKey(courseName)) {
            val details = detailsMap.get(courseName)
            details?.let {
                actionDispatcher(UpdateAction.UpdateCourseDetailsAction(details))
            }
        }
    }
}
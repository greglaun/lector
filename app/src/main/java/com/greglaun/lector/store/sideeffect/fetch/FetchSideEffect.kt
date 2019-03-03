package com.greglaun.lector.store.sideeffect.fetch

import com.greglaun.lector.data.cache.ResponseSource
import com.greglaun.lector.data.course.CourseDownloader
import com.greglaun.lector.data.course.CourseSource
import com.greglaun.lector.store.Action
import com.greglaun.lector.store.ReadAction
import com.greglaun.lector.store.SideEffect
import com.greglaun.lector.store.Store

class FetchSideEffect(val store: Store, val responseSource: ResponseSource,
                      val courseSource: CourseSource,
                      val courseDownloader: CourseDownloader): SideEffect {

    init {
        store.sideEffects.add(this)
    }

    override suspend fun handle(action: Action) {
        when (action) {
            is ReadAction.FetchCourseDetailsAction ->
                fetchCourseDetails(action, courseDownloader) { store.dispatch(it) }
        }
    }
}
package com.greglaun.lector.store

import androidx.annotation.RequiresPermission
import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.data.course.ThinCourseDetails
import com.greglaun.lector.ui.speak.AbstractArticleState
import com.greglaun.lector.ui.speak.SpeakerState

sealed class Action

sealed class UpdateAction: Action() {
    data class UpdateArticleAction(val articleState: AbstractArticleState): UpdateAction()
    data class NewArticleAction(val articleState: AbstractArticleState): UpdateAction()
    data class UpdateNavigationAction(val navigation: Navigation): UpdateAction()
    data class UpdateCourseDetailsAction(val courseDetails: ThinCourseDetails): UpdateAction()
    data class UpdateSpeakerStateAction(val speakerState: SpeakerState): UpdateAction()
    class FastForwardOne: UpdateAction()
    class RewindOne: UpdateAction()
}

sealed class SpeakerAction: Action() {
    class StopSpeakingAction: SpeakerAction()
}

sealed class ReadAction: Action() {
    data class FetchCourseDetailsAction(val courseContext: CourseContext) : ReadAction()
    class StartDownloadAction: ReadAction()
    class StopDownloadAction: ReadAction()
    data class LoadNewUrlAction(val newUrl: String): ReadAction()
}



// Starting up action
// Winding down action
// Link clicked
// Settings change in UI
// Fast forward
// Rewind
// Play
// Pause
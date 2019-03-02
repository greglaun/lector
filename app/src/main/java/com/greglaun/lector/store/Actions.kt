package com.greglaun.lector.store

import com.greglaun.lector.data.course.CourseContext
import com.greglaun.lector.ui.speak.AbstractArticleState

sealed class Action
data class UpdateArticleAction(val articleState: AbstractArticleState): Action()
sealed class ReadAction: Action() {
    data class FetchCourseDetailsAction(val courseContext: CourseContext) : ReadAction()
}
// Starting up action
// Winding down action
// Link clicked
// Settings change in UI
// Fast forward
// Rewind
// Play
// Pause
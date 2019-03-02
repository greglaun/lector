package com.greglaun.lector.store

fun reduceArticleAction(action: UpdateArticleAction, currentState: State): State {
    return currentState.updateArticleScreen(CurrentArticleScreen(
            action.articleState,
            currentState.currentArticleScreen.currentCourse,
            currentState.currentArticleScreen.speakerState))
}

fun reduceFetchCourseDetailsAction(action: ReadAction.FetchCourseDetailsAction,
                                    currentState: State): State {
    return currentState.updateReadingListScreen(
            ReadingListScreen(action.courseContext.courseName,
            Lce.Loading))

}
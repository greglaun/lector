package com.greglaun.lector.store

import com.greglaun.lector.data.cache.BasicArticleContext

fun reduceUpdateArticleAction(action: UpdateAction.UpdateArticleAction, currentState: State): State {
    return currentState.updateArticleScreen(CurrentArticleScreen(
            action.articleState,
            currentState.currentArticleScreen.currentCourse,
            currentState.currentArticleScreen.speakerState))
}

fun reduceUpdateCourseDetailsAction(action :UpdateAction.UpdateCourseDetailsAction,
                                    currentState: State): State {
    return if (action.courseDetails.name == currentState.readingListScreen.currentReadingList) {
        // We have details for the proper list
        currentState.updateReadingListScreen(
                ReadingListScreen(currentState.readingListScreen.currentReadingList,
                        Lce.Success(action.courseDetails.articleNames.map { it ->
                            BasicArticleContext.fromString(it)
                        })
                )
        )
    } else {
        currentState
    }
}

fun reduceFetchCourseDetailsAction(action: ReadAction.FetchCourseDetailsAction,
                                    currentState: State): State {
    return currentState.updateReadingListScreen(
            ReadingListScreen(action.courseContext.courseName,
            Lce.Loading))
}


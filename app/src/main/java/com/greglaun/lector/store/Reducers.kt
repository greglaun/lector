package com.greglaun.lector.store

fun reduceArticleAction(action: UpdateArticleAction, currentState: State): State {
    return currentState.updateArticleScreen(CurrentArticleScreen(
            currentState.currentArticleScreen.currentContext,
            currentState.currentArticleScreen.currentCourse,
            action.articleState,
            currentState.currentArticleScreen.speakerState))
}


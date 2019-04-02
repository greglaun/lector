package com.greglaun.lector.store

import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.data.cache.SavedArticleCache
import com.greglaun.lector.ui.speak.hasNext
import com.greglaun.lector.ui.speak.hasPrevious
import com.greglaun.lector.ui.speak.next
import com.greglaun.lector.ui.speak.previous

fun reduceUpdateArticleAction(action: UpdateAction.UpdateArticleAction, currentState: State,
                              isNew: Boolean = false): State {
    val oldSpeakerState = currentState.speakerState
    val newSpeakerState =
            if (oldSpeakerState == SpeakerState.NOT_READY) SpeakerState.READY else oldSpeakerState
    return currentState.updateArticleScreen(CurrentArticleScreen(
            action.articleState,
            currentState.currentArticleScreen.currentCourse), newSpeakerState)
}


fun reduceNewArticleAction(action: UpdateAction.NewArticleAction, currentState: State): State {
    val oldSpeakerState = currentState.speakerState
    val newSpeakerState =
            if (oldSpeakerState == SpeakerState.NOT_READY) SpeakerState.READY else oldSpeakerState
    val newState = currentState.newArticleScreen(CurrentArticleScreen(
            action.articleState,
            currentState.currentArticleScreen.currentCourse,
            true), newSpeakerState)
    return newState.updateNavigation(Navigation.CURRENT_ARTICLE)
}

fun reduceUpdateNavigationAction(action: UpdateAction.UpdateNavigationAction,
                                 currentState: State): State {
    return currentState.updateNavigation(action.navigation)
}

fun reduceFastForwardOne(action: UpdateAction.FastForwardOne, currentState: State): State {
    val articleState = currentState.currentArticleScreen.articleState
    if (articleState.hasNext()) {
        return currentState.updateArticleScreen(CurrentArticleScreen(
                articleState.next()!!,
                currentState.currentArticleScreen.currentCourse),
                maybeNew(currentState.speakerState))
    }
    return currentState
}

fun reduceRewindOne(action: UpdateAction.RewindOne, currentState: State): State {
    val articleState = currentState.currentArticleScreen.articleState
    if (articleState.hasPrevious()) {
        return currentState.updateArticleScreen(CurrentArticleScreen(
                articleState.previous()!!,
                currentState.currentArticleScreen.currentCourse),
                maybeNew(currentState.speakerState))
    }
    return currentState
}

fun reduceUpdateCourseDetailsAction(action: UpdateAction.UpdateCourseDetailsAction,
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

fun reduceUpdateSpeakerState(action: UpdateAction.UpdateSpeakerStateAction,
                             currentState: State): State {
    return currentState.updateSpeakerState(action.speakerState)
}

fun reduceStopSpeakingAction(action: Action, currentState: State): State {
    when (currentState.speakerState) {
        SpeakerState.SPEAKING -> return currentState.updateSpeakerState(SpeakerState.READY)
        SpeakerState.SPEAKING_NEW_UTTERANCE -> return currentState.updateSpeakerState(SpeakerState.READY)
        else -> return currentState // READY -> READY, NOT_READY -> NOT_READY
    }
}

fun reduceSpeakAction(action: SpeakerAction.SpeakAction, currentState: State): State {
    return currentState.updateSpeakerState(SpeakerState.SPEAKING_NEW_UTTERANCE)
}

fun reduceArticleOverAction(action: UpdateAction.ArticleOverAction, currentState: State): State {
    return reduceStopSpeakingAction(action, currentState)
}

fun reduceUpdateArticleFreshnessState(action: UpdateAction.UpdateArticleFreshnessAction,
                                      currentState: State): State {
    if (action.articleState == currentState.currentArticleScreen.articleState) {
        return currentState.updateArticleScreen(
                CurrentArticleScreen(
                currentState.currentArticleScreen.articleState,
                currentState.currentArticleScreen.currentCourse,
                false), currentState.speakerState)
    }
    return currentState
}

fun reduceFetchAllPermanentAndDisplay(action: ReadAction.FetchAllPermanentAndDisplay, state: State): State {
    val newState = state.updateReadingListScreen(ReadingListScreen(
            articles = Lce.Loading))
    return newState.updateNavigation(Navigation.MY_READING_LIST)
}

fun reduceUpdateReadingList(action: UpdateAction.UpdateReadingListAction, state: State): State {
    return state.updateReadingListScreen(ReadingListScreen(
            articles = action.readingListLce))
}

fun reduceUpdateCourseList(action: UpdateAction.UpdateCourseListAction, state: State): State {
    return state.updateCourseBrowserScreen(CourseBrowserScreen(action.courseListLce))
}

fun reduceSetHandsomeBritish(action: PreferenceAction.SetHandsomeBritish, state: State): State {
    return state.updatePreferences(Preferences(
            state.preferences.autoPlay,
            state.preferences.autoDelete,
            action.shouldBeBritish,
            state.preferences.isSlow,
            state.preferences.speechRate
    ))
}

fun reduceSetSpeechRate(action: PreferenceAction.SetSpeechRate, state: State): State {
    return state.updatePreferences(Preferences(
            state.preferences.autoPlay,
            state.preferences.autoDelete,
            state.preferences.isBritish,
            state.preferences.isSlow,
            action.speechRate
    ))
}

fun reduceSetAutoPlay(action: PreferenceAction.SetAutoPlay, state: State): State {
    return state.updatePreferences(Preferences(
            action.isAutoPlay,
            state.preferences.autoDelete,
            state.preferences.isBritish,
            state.preferences.isSlow,
            state.preferences.speechRate
    ))
}

fun reduceSetAutoDelete(action: PreferenceAction.SetAutoDelete, state: State): State {
    return state.updatePreferences(Preferences(
            state.preferences.autoPlay,
            action.isAutoDelete,
            state.preferences.isBritish,
            state.preferences.isSlow,
            state.preferences.speechRate
    ))
}

fun reduceSetIsSlow(action: PreferenceAction.SetIsSlow, state: State): State {
    return state.updatePreferences(Preferences(
            state.preferences.autoPlay,
            state.preferences.autoDelete,
            state.preferences.isBritish,
            action.isSlow,
            state.preferences.speechRate
    ))
}


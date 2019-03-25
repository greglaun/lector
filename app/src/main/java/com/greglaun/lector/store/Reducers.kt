package com.greglaun.lector.store

import com.greglaun.lector.data.cache.BasicArticleContext
import com.greglaun.lector.ui.speak.*

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
    return currentState.newArticleScreen(CurrentArticleScreen(
            action.articleState,
            currentState.currentArticleScreen.currentCourse), newSpeakerState)
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

fun reduceStartDowloadAction(action: ReadAction.StartDownloadAction,
                             currentState: State): State {
    return currentState
}

fun reduceUpdateSpeakerState(action: UpdateAction.UpdateSpeakerStateAction,
                             currentState: State): State {
    return currentState.updateSpeakerState(action.speakerState)
}

fun reduceStopSpeakingAction(action: SpeakerAction.StopSpeakingAction, currentState: State): State {
    when (currentState.speakerState) {
        SpeakerState.SPEAKING -> return currentState.updateSpeakerState(SpeakerState.READY)
        SpeakerState.SPEAKING_NEW_UTTERANCE -> return currentState.updateSpeakerState(SpeakerState.READY)
        else -> return currentState // READY -> READY, NOT_READY -> NOT_READY
    }
}

fun reduceSpeakAction(action: SpeakerAction.SpeakAction, currentState: State): State {
//    ttsClient.speechViewSpeak(cleanUtterance(text), utteranceId(text)) {
//        if (it == utteranceId(text)) {
//            ttsStateListener.onUtteranceEnded(articleState!!)
//            if (articleState!!.hasNext()) {
//                runBlocking {
//                    store.dispatch(UpdateAction.FastForwardOne())
//                }
//            } else {
//                runBlocking {
//                    store.dispatch(UpdateAction.UpdateSpeakerStateAction(
//                            SpeakerState.NOT_READY))
//                    ttsStateListener.onSpeechStopped()
//                    ttsStateListener.onArticleFinished(articleState!!)
//                }
//            }
//        }
//    }
    return currentState.updateSpeakerState(SpeakerState.SPEAKING_NEW_UTTERANCE)
}
package com.greglaun.lector.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This class implements the functional programming idiom of giant-ball-of-state that gets passed
 * around. This idiom has become popular in web frameworks, and the architecture I'm starting with
 * is heavily inspired by a blog post about "unidirectional data flow" in Android:
 *
 * https://proandroiddev.com/unidirectional-data-flow-on-android-the-blog-post-part-1-cadcf88c72f5
 * https://proandroiddev.com/unidirectional-data-flow-on-android-the-blog-post-part-2-b8cfedb1265a
 *
 * See also the corresponding source code for the above blog post here:
 * https://github.com/CesarValiente/KUnidirectional/
 */

abstract class Store {
    private val storeContext = newSingleThreadContext("StoreContext")
    val stateHandlers: CopyOnWriteArrayList<StateHandler> = CopyOnWriteArrayList()
    val sideEffects: CopyOnWriteArrayList<SideEffect> = CopyOnWriteArrayList()

    var state = State()
        protected set

    private val stateActor =
            CoroutineScope(storeContext).actor<Action>(
                    Dispatchers.Default, 2, CoroutineStart.DEFAULT, null, {
                for (msg in channel) {
                    handle(msg)
                }
            })

    suspend fun dispatch(action: Action) {
        stateActor.send(action)
    }

    private suspend fun handle(action: Action) {
        val newState = reduce(action, state)
        dispatch(newState)
        sideEffects.forEach { it.handle(action)}
    }

    private suspend fun dispatch(state: State) {
        this.state = state
        stateHandlers.forEach { it.handle(state) }
    }

    private fun reduce(action: Action, currentState: State): State {
        return when (action) {
            is UpdateAction.NewArticleAction -> reduceNewArticleAction(action, state)
            is UpdateAction.UpdateArticleAction -> reduceUpdateArticleAction(action, state)
            is UpdateAction.UpdateNavigationAction -> reduceUpdateNavigationAction(action, state)
            is UpdateAction.ForwardOne -> reduceFastForwardOne(state)
            is UpdateAction.RewindOne -> reduceRewindOne(state)
            is UpdateAction.UpdateCourseDetailsAction ->
                reduceUpdateCourseDetailsAction(action, state)
            is UpdateAction.ArticleOverAction -> reduceArticleOverAction(state)
            is UpdateAction.UpdateSpeakerStateAction -> reduceUpdateSpeakerState(action, state)
            is UpdateAction.UpdateArticleFreshnessAction ->
                reduceUpdateArticleFreshnessState(action, state)
            is UpdateAction.UpdateReadingListAction -> reduceUpdateReadingList(action, state)
            is UpdateAction.UpdateSavedCoursesAction -> reduceUpdateSavedCoursesAction(
                    action, state)
            is UpdateAction.UpdateCourseBrowseList -> reduceUpdateCourseBrowseList(action, state)
            is ReadAction.FetchCourseDetailsAction -> reduceFetchCourseDetailsAction(action, state)
            is ReadAction.FetchAllPermanentAndDisplay -> reduceFetchAllPermanentAndDisplay(state)
            is SpeakerAction.SpeakAction -> reduceSpeakAction(state)
            is SpeakerAction.StopSpeakingAction -> reduceStopSpeakingAction(state)
            is PreferenceAction.SetHandsomeBritish -> reduceSetHandsomeBritish(action, state)
            is PreferenceAction.SetSpeechRate -> reduceSetSpeechRate(action, state)
            is PreferenceAction.SetAutoPlay -> reduceSetAutoPlay(action, state)
            is PreferenceAction.SetAutoDelete -> reduceSetAutoDelete(action, state)
            is PreferenceAction.SetIsSlow -> reduceSetIsSlow(action, state)
            else -> return currentState
        }
    }
}
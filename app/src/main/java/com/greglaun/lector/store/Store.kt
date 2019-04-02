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
        val newState = when (action) {
            is UpdateAction.NewArticleAction -> reduceNewArticleAction(action, state)
            is UpdateAction.UpdateArticleAction -> reduceUpdateArticleAction(action, state)
            is UpdateAction.UpdateNavigationAction -> reduceUpdateNavigationAction(action, state)
            is UpdateAction.FastForwardOne -> reduceFastForwardOne(action, state)
            is UpdateAction.RewindOne -> reduceRewindOne(action, state)
            is UpdateAction.UpdateCourseDetailsAction ->
                reduceUpdateCourseDetailsAction(action, state)
            is UpdateAction.ArticleOverAction -> reduceArticleOverAction(action, state)
            is UpdateAction.UpdateSpeakerStateAction -> reduceUpdateSpeakerState(action, state)
            is UpdateAction.UpdateArticleFreshnessAction ->
                reduceUpdateArticleFreshnessState(action, state)
            is UpdateAction.UpdateReadingListAction -> reduceUpdateReadingList(action, state)
            is UpdateAction.UpdateCourseListAction -> reduceUpdateCourseList(action, state)
            is ReadAction.FetchCourseDetailsAction -> reduceFetchCourseDetailsAction(action, state)
            is ReadAction.FetchAllPermanentAndDisplay -> reduceFetchAllPermanentAndDisplay(action,
                    state)
            is SpeakerAction.SpeakAction -> reduceSpeakAction(action, state)
            is SpeakerAction.StopSpeakingAction -> reduceStopSpeakingAction(action, state)
            is PreferenceAction.SetHandsomeBritish -> reduceSetHandsomeBritish(action, state)
            is PreferenceAction.SetSpeechRate -> reduceSetSpeechRate(action, state)
            is PreferenceAction.SetAutoPlay -> reduceSetAutoPlay(action, state)
            is PreferenceAction.SetAutoDelete -> reduceSetAutoDelete(action, state)
            is PreferenceAction.SetIsSlow -> reduceSetIsSlow(action, state)
//            is CreationAction -> CreationReducer.reduce(action, currentState)
//            is UpdateAction -> UpdateReducer.reduce(action, currentState)
//            is ReadAction -> ReadReducer.reduce(action, currentState)
//            is DeleteAction -> DeleteReducer.reduce(action, currentState)
//            is NavigationAction -> NavigationReducer.reduce(action, currentState)
            else -> return currentState
        }
        return newState
    }
}
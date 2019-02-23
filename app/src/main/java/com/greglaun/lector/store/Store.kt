package com.greglaun.lector.store

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.newSingleThreadContext
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
                    Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
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
            is UpdateArticleAction -> reduceArticleAction(action, state)
//            is CreationAction -> CreationReducer.reduce(action, currentState)
//            is UpdateAction -> UpdateReducer.reduce(action, currentState)
//            is ReadAction -> ReadReducer.reduce(action, currentState)
//            is DeleteAction -> DeleteReducer.reduce(action, currentState)
//            is NavigationAction -> NavigationReducer.reduce(action, currentState)
        }
        return newState
    }
}
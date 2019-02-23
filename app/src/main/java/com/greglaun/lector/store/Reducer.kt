package com.greglaun.lector.store

interface Reducer {
    fun reduce(action: Action, currentState: State): State
}

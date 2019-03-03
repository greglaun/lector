package com.greglaun.lector.store

interface StateHandler {
    suspend fun handle(state: State)
}
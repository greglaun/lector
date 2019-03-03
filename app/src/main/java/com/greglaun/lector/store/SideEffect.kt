package com.greglaun.lector.store

interface SideEffect {
    suspend fun handle(action: Action)
}
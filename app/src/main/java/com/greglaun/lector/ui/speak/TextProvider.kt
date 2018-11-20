package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.Deferred

interface TextProvider {
    val title: String
    fun onUrlChanged(urlString: String): Deferred<Unit>
    fun getCurrent(): String?
    fun advance()
    fun fastForwardTo(place: String): Boolean
    fun articleIsOver(): Boolean
}

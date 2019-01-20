package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.Deferred

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener)
    fun stopMachine()

    fun getState(): Deferred<SpeakerState>

    fun changeStateReady(): Deferred<Unit>
    suspend fun changeStateUpdateArticle(articleState: ArticleState)
    fun changeStateStartSpeaking(): Deferred<Unit>

    fun actionSpeakOne(): Deferred<SpeakerState>
    fun actionStopSpeaking(): Deferred<Unit?>
    fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit>
    suspend fun actionChangeUrl(articleState: ArticleState)
    fun actionGetPosition(): Deferred<String>

    fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
    fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
}
package com.greglaun.lector.ui.speak

import kotlinx.coroutines.experimental.Deferred

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener)
    fun stopMachine()

    suspend fun updateArticle(articleState: ArticleState)

    fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?): Deferred<Unit>
    fun getSpeakerState(): Deferred<SpeakerState>
    fun actionSpeakOne(): Deferred<SpeakerState>
    fun actionStopSpeaking(): Deferred<Unit?>
    fun getArticleState(): Deferred<ArticleState>

    fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
    fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit): Deferred<Unit>
}
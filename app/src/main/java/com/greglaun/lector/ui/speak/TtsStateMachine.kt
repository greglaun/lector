package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.Store

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener, store: Store)
    fun stopMachine()

    suspend fun updateArticle(articleState: ArticleState)

    suspend fun actionSpeakInLoop(onPositionUpdate: ((ArticleState) -> Unit)?)
    suspend fun getSpeakerState(): SpeakerState
    suspend fun actionSpeakOne(): SpeakerState
    suspend fun actionStopSpeaking()
    suspend fun getArticleState(): ArticleState

    suspend fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit)
    suspend fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit)
}
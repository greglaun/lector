package com.greglaun.lector.ui.speak

interface TtsStateMachine {
    fun startMachine(ttsActorClient: TtsActorClient, stateListener: TtsStateListener)
    fun stopMachine()

    suspend fun updateArticle(articleState: ArticleState)

    suspend fun actionSpeakInLoop(onPositionUpdate: ((String) -> Unit)?)
    suspend fun getSpeakerState(): SpeakerState
    suspend fun actionSpeakOne(): SpeakerState
    suspend fun actionStopSpeaking()
    suspend fun getArticleState(): ArticleState

    suspend fun stopAdvanceOneAndResume(onDone: (ArticleState) -> Unit)
    suspend fun stopReverseOneAndResume(onDone: (ArticleState) -> Unit)
}
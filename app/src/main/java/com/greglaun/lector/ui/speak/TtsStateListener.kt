package com.greglaun.lector.ui.speak

interface TtsStateListener {
    fun onUtteranceStarted(articleState: ArticleState)
    fun onUtteranceEnded(articleState: ArticleState)
    suspend fun onArticleFinished(articleState: ArticleState)
    fun onSpeechStopped()
}
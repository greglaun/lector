package com.greglaun.lector.ui.speak

interface TtsStateListener {
    suspend fun onArticleFinished(articleState: ArticleState)
    fun onSpeechStopped()
}
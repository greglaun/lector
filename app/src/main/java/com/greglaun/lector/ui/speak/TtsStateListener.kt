package com.greglaun.lector.ui.speak

interface TtsStateListener {
    fun onUtteranceStarted(articleState: ArticleState)
    fun onUtteranceEnded(articleState: ArticleState)
    fun onSpeechStopped()
}
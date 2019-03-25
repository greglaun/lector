package com.greglaun.lector.ui.speak

interface TtsActorClient {
    suspend fun speechViewSpeak(text: String, utteranceId: String,
                                callback: suspend (String) -> Unit)
    fun stopSpeechViewImmediately()
}
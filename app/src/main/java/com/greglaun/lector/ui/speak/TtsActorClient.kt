package com.greglaun.lector.ui.speak

interface TtsActorClient {
    fun speechViewSpeak(text: String, utteranceId: String, callback: (String) -> Unit)
    fun stopSpeechViewImmediately()
}
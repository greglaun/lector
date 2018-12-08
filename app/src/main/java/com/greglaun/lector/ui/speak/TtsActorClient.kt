package com.greglaun.lector.ui.speak

interface TtsActorClient {
    fun speechViewSpeak(text: String, callback: (String) -> Unit)
    fun stopSpeechViewImmediately()
}
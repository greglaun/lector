package com.greglaun.lector.data.model.speakable

interface Speakable {
    fun startSpeaking(system : SpeechSystem)
    fun stopSpeaking(system : SpeechhSystem)
}
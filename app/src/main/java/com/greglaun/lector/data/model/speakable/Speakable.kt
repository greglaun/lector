package com.greglaun.lector.data.model.speakable

import com.greglaun.lector.data.model.speechsystem.SpeechSystem

interface Speakable {
    fun startSpeaking(system : SpeechSystem)
    fun stopSpeaking(system : SpeechSystem)
}
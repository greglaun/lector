package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.model.speechsystem.UtteranceIdentifier

interface SpeakView {
    fun startSpeaking()
    fun stopSpeaking()
    fun nextUtterance()
    fun previousUttterance()
    fun jumpToUtterance(utteranceIdentifier: UtteranceIdentifier)
}
package com.greglaun.lector.ui.speak

interface {
    startSpeaking()
    stopSpeaking()
    nextUtterance()
    previousUttterance()
    jumpToUtterance(UtteranceIdentifier)
}
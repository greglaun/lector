package com.greglaun.lector.data.model.speakable

import com.greglaun.lector.data.model.speechsystem.UtteranceIdentifier

interface Seekable {
    fun nextUtterance()
    fun previousUttterance()
    fun jumpToUtterance(id : UtteranceIdentifier)
}
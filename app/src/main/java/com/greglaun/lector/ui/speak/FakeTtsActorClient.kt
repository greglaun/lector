package com.greglaun.lector.ui.speak

import com.greglaun.lector.data.cache.utteranceId

class FakeTtsActorClient : TtsActorClient {
    override fun speechViewSpeak(text: String, utteranceId: String, callback: (String) -> Unit) {
        callback(utteranceId(text))
    }

    override fun stopSpeechViewImmediately() {
        // Do nothing
    }
}
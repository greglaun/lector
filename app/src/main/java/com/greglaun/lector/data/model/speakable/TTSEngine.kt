package com.greglaun.lector.data.model.speakable

// This is an attempt at the MVP
interface TTSContract {
    interface AudioView { // Surely there's a better name.
        fun speak(textToSpeak : String) // Android call: tts.speak(text, TextToSpeech.QUEUE_ADD, null, "UniqueID");
        fun stop()
    }

    interface TTSPresenter {

    }

}
package com.greglaun.lector.android

import android.speech.tts.TextToSpeech
import com.greglaun.lector.ui.speak.TTSContract

class AndroidAudioView() : TTSContract.AudioView, TextToSpeech.OnInitListener{
    var androidTts : TextToSpeech? = null

    override fun onInit(status: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun speak(textToSpeak: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
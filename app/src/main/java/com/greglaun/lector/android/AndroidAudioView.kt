package com.greglaun.lector.android

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.greglaun.lector.data.cache.utteranceId
import com.greglaun.lector.ui.speak.TTSContract
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AndroidAudioView(private val androidTts : TextToSpeech) : TTSContract.AudioView,
        UtteranceProgressListener() {
    private val callbacks : HashMap<String, suspend (String) -> Unit> = HashMap()
    override suspend fun speak(textToSpeak: String, utteranceId: String,
                               callback : suspend (String)-> Unit) {
        if (textToSpeak == "") {
            callback(utteranceId(""))
            return
        }
        callbacks[utteranceId] = callback
        androidTts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null,
                utteranceId)
    }

    override fun stopImmediately() {
        androidTts.playSilentUtterance(10, TextToSpeech.QUEUE_FLUSH, null)
    }

    @Deprecated("Deprecated in UtteranceProgressListener")
    override fun onError(utteranceId: String?) {
        // Do nothing
    }

    override fun onStart(utteranceId: String?) {
        // Do nothing
    }

    override fun onDone(utteranceId: String?) {
        if (utteranceId != null) {
            val callback = callbacks[utteranceId]
            if (callback != null) {
                GlobalScope.launch {
                    callback(utteranceId)
                    callbacks.remove(utteranceId)
                }
            }
        }
    }

    override fun setHandsomeBritish(shouldBeBritish: Boolean) {
        if (!shouldBeBritish) {
            androidTts.voice = androidTts.defaultVoice
            androidTts.setSpeechRate(2.7f)
            return
        }
        val britishVoices = mutableListOf<Voice>()
        var britishVoice: Voice? = null
        val voices = androidTts.voices
        for (voice in voices) {
            if (voice.name == "en-GB-language") {
                britishVoice = voice
                continue
            }
            if (voice.locale == Locale.UK && !voice.isNetworkConnectionRequired &&
                    voice.quality >= Voice.QUALITY_VERY_HIGH) {
                britishVoices.add(voice)
            }
        }
        if (britishVoice != null) {
            androidTts.voice = britishVoice
            androidTts.setSpeechRate(1.0f)
        } else {
            if (britishVoices.size != 0) {
                androidTts.voice = britishVoices[0] // Just set any old British voiceo
                androidTts.setSpeechRate(1.0f)
            }
        }
    }

    override fun setSpeechRate(speechRate: Float) {
        androidTts.setSpeechRate(speechRate/100.0f)
    }
}
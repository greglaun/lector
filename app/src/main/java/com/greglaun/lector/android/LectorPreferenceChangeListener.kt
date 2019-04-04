package com.greglaun.lector.android

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.greglaun.lector.ui.main.MainContract
import com.greglaun.lector.ui.speak.TTSContract

class LectorPreferenceChangeListener(val mainPresenter: MainContract.Presenter,
                                     val ttsPresenter: TTSContract.Presenter):
        SharedPreferences.OnSharedPreferenceChangeListener, LectorPreferenceManager {
    val AUTOPLAY_DEFAULT = true
    val AUTODELETE_DEFAULT = true

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && sharedPreferences != null) {
            when(key) {
                "british_voice" -> {
                    sharedPreferences.getBoolean(key, false)?.let {
                        ttsPresenter.setHandsomeBritish(it)
                    }
                }
                "tts_speed" -> {
                    sharedPreferences.getInt(key, 100).let{
                        ttsPresenter.setSpeechRate(it.toFloat())
                    }
                }
                "auto_play" -> {
                    sharedPreferences.getBoolean(key, false)?.let {
                        mainPresenter.setAutoPlay(it)
                    }
                }
                "auto_delete" -> {
                    sharedPreferences.getBoolean(key, false)?.let {
                        mainPresenter.setAutoDelete(it)
                    }
                }
            }
        }
    }

    override fun setFromPreferences(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val shouldBeBritish = sharedPreferences.getBoolean("british_voice", false)
        if (shouldBeBritish) {
            ttsPresenter.setHandsomeBritish(shouldBeBritish)
        }

        val speechRate = sharedPreferences.getInt("tts_speed", 100)
        ttsPresenter.setSpeechRate(speechRate.toFloat())

        val autoPlay = sharedPreferences.getBoolean("auto_play", AUTOPLAY_DEFAULT)
        mainPresenter.setAutoPlay(autoPlay)

        val autoDelete = sharedPreferences.getBoolean("auto_delete", AUTODELETE_DEFAULT)
        mainPresenter.setAutoDelete(autoDelete)
    }
}
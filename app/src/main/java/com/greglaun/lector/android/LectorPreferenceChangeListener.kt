package com.greglaun.lector.android

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.greglaun.lector.ui.main.MainContract

class LectorPreferenceChangeListener(val mainPresenter: MainContract.Presenter):
        SharedPreferences.OnSharedPreferenceChangeListener, LectorPreferenceManager {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && sharedPreferences != null) {
            when(key) {
                "british_voice" -> {
                    sharedPreferences.getBoolean(key, false)?.let {
                        mainPresenter.setHandsomeBritish(it)
                    }
                }
                "tts_speed" -> {
                    sharedPreferences.getInt(key, 100).let{
                        mainPresenter.setSpeechRate(it.toFloat())
                    }
                }
            }
        }
    }

    override fun setFromPreferences(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val shouldBeBritish = sharedPreferences.getBoolean("british_voice", false)
        if (shouldBeBritish) {
            mainPresenter.setHandsomeBritish(shouldBeBritish)
        }

        val speechRate = sharedPreferences.getInt("tts_speed", 100)
        mainPresenter.setSpeechRate(speechRate.toFloat())
    }
}
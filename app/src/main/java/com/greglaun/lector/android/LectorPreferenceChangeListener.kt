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
            }
        }
    }

    override fun setFromPreferences(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val shouldBeBritish = sharedPreferences.getBoolean("british_voice", false)
        if (shouldBeBritish) {
            mainPresenter.setHandsomeBritish(shouldBeBritish)
        }
    }
}